package org.jenkinsci.deprecatedusage;

import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.util.TimeValue;
import org.json.JSONObject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipException;

public class Main {

    public static void main(String[] args) throws Exception {
        new Main().doMain(args);
    }

    public void doMain(String[] args) throws Exception {

        final Options options = Options.get();
        final CmdLineParser commandLineParser = new CmdLineParser(options);
        try {
            commandLineParser.parseArgument(args);
        } catch (CmdLineException e) {
            commandLineParser.printUsage(System.err);
            System.exit(1);
        }

        if (options.help) {
            commandLineParser.printUsage(System.err);
            System.exit(0);
        }
        final long start = System.currentTimeMillis();
        final ExecutorService threadPool = Executors.newCachedThreadPool();
        HttpRequestRetryStrategy retryStrategy = new DefaultHttpRequestRetryStrategy(3, TimeValue.ofSeconds(5));
        try (CloseableHttpAsyncClient client = HttpAsyncClients.custom().setRetryStrategy(retryStrategy).build()) {
            final DeprecatedApi deprecatedApi = new DeprecatedApi();
            addClassesToAnalyze(deprecatedApi);
            List<String> updateCenterURLs = options.getUpdateCenterURLs();
            CountDownLatch metadataLoaded = new CountDownLatch(updateCenterURLs.size());
            Set<JenkinsFile> cores = new ConcurrentSkipListSet<>(Comparator.comparing(JenkinsFile::getFile));
            Set<JenkinsFile> plugins = new ConcurrentSkipListSet<>(Comparator.comparing(JenkinsFile::getFile));
            client.start();
            for (String updateCenterURL : updateCenterURLs) {
                System.out.println("Using update center URL: " + updateCenterURL);
                SimpleHttpRequest request = SimpleHttpRequests.get(updateCenterURL);
                client.execute(request, new FutureCallback<SimpleHttpResponse>() {
                    @Override
                    public void completed(SimpleHttpResponse result) {
                        System.out.println("Downloaded " + updateCenterURL);
                        try {
                            handleBodyText(result.getBodyText());
                        } catch (RuntimeException e) {
                            System.out.println("Error downloading " + updateCenterURL);
                            System.out.println(e.toString());
                        } finally {
                            metadataLoaded.countDown();
                        }
                    }

                    private void handleBodyText(String bodyText) {
                        String json = bodyText.replace("updateCenter.post(", "");
                        JSONObject jsonRoot = new JSONObject(json);
                        UpdateCenter updateCenter = new UpdateCenter(jsonRoot);
                        cores.add(updateCenter.getCore());
                        plugins.addAll(updateCenter.getPlugins());
                    }

                    @Override
                    public void failed(Exception ex) {
                        System.out.println("Error downloading update center metadata for " + updateCenterURL);
                        System.out.println(ex.toString());
                        metadataLoaded.countDown();
                    }

                    @Override
                    public void cancelled() {
                        System.out.println("Cancelled download of " + updateCenterURL);
                    }
                });
            }

            // wait for async code to finish submitting
            metadataLoaded.await(15, TimeUnit.SECONDS);
            System.out.println("Downloading core files");
            CompletableFuture<Void> coreAnalysisComplete = CompletableFuture.allOf(
                    cores.stream()
                            .map(core -> core.downloadIfNotExists(client, threadPool).thenRun(() -> {
                                try {
                                    System.out.println("Analyzing deprecated APIs in " + core);
                                    deprecatedApi.analyze(core.getFile());
                                    System.out.println("Finished deprecated API analysis in " + core);
                                } catch (IOException e) {
                                    System.out.println("Error analyzing deprecated APIs in " + core);
                                    System.out.println(e.toString());
                                }
                            }))
                            .toArray(CompletableFuture[]::new));

            System.out.println("Downloading plugin files");
            int pluginCount = plugins.size();
            int maxConcurrent = Math.max(options.maxConcurrentDownloads, Runtime.getRuntime().availableProcessors() * 4);
            Semaphore concurrentDownloadsPermit = new Semaphore(maxConcurrent);
            List<JenkinsFile> downloadedPlugins = Collections.synchronizedList(new ArrayList<>(pluginCount));
            List<CompletableFuture<?>> futures = new ArrayList<>(pluginCount + 1);
            futures.add(coreAnalysisComplete);
            for (JenkinsFile plugin : plugins) {
                concurrentDownloadsPermit.acquire();
                futures.add(plugin.downloadIfNotExists(client, threadPool).handle((success, failure) -> {
                    concurrentDownloadsPermit.release();
                    if (failure != null) {
                        if (failure instanceof ConnectionClosedException) {
                            System.out.println("Gave up trying to download " + plugin);
                        } else {
                            System.out.println("Error downloading " + plugin);
                            System.out.println(failure.toString());
                        }
                    } else {
                        downloadedPlugins.add(plugin);
                    }
                    return null;
                }));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            System.out.println("Analyzing usage in plugins");
            final List<DeprecatedUsage> deprecatedUsages = analyzeDeprecatedUsage(downloadedPlugins, deprecatedApi, threadPool);

            Report[] reports = new Report[]{
                    new DeprecatedUsageByPluginReport(deprecatedApi, deprecatedUsages, new File("output"), "usage-by-plugin"),
                    new DeprecatedUnusedApiReport(deprecatedApi, deprecatedUsages, new File("output"), "deprecated-and-unused"),
                    new DeprecatedUsageByApiReport(deprecatedApi, deprecatedUsages, new File("output"), "usage-by-api")
            };

            for (Report report : reports) {
                report.generateJsonReport();
                report.generateHtmlReport();
            }

            System.out.println("duration : " + (System.currentTimeMillis() - start) + " ms at "
                    + DateFormat.getDateTimeInstance().format(new Date()));
        } finally {
            threadPool.shutdown();
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                System.out.println("Timed out waiting for thread pool to cleanly exit");
            }
        }
    }

    /**
     * Adds hardcoded classes to analyze for usage. This is mostly designed for finding classes planned for deprecation,
     * but can be also used to find any class usage.
     *
     */
    private static void addClassesToAnalyze(DeprecatedApi deprecatedApi) throws IOException {
        if (Options.get().additionalClassesFile != null) {
            List<String> additionalClasses = Options.getAdditionalClasses();
            deprecatedApi.addClasses(additionalClasses);
        } else {
            System.out.println("No 'additionalClassesFile' option, only already deprecated class will be searched for");
        }
    }

    private static List<DeprecatedUsage> analyzeDeprecatedUsage(List<JenkinsFile> plugins, DeprecatedApi deprecatedApi,
                                                                Executor executor)
            throws InterruptedException, ExecutionException {
        List<CompletableFuture<DeprecatedUsage>> futures = new ArrayList<>();
        for (JenkinsFile plugin : plugins) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                DeprecatedUsage deprecatedUsage = new DeprecatedUsage(plugin.getName(), plugin.getVersion(), deprecatedApi);
                try {
                    deprecatedUsage.analyze(plugin.getFile());
                } catch (final EOFException | ZipException e) {
                    System.out.println("deleting " + plugin + " and skipping, because " + e.toString());
                    try {
                        plugin.deleteFile();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } catch (final Exception e) {
                    System.out.println(e.toString() + " on " + plugin.getFile().getName());
                    e.printStackTrace();
                }
                return deprecatedUsage;
            }, executor));
        }

        final List<DeprecatedUsage> deprecatedUsages = new ArrayList<>();
        int i = 0;
        for (final Future<DeprecatedUsage> future : futures) {
            final DeprecatedUsage deprecatedUsage = future.get();
            deprecatedUsages.add(deprecatedUsage);
            i++;
            if (i % 10 == 0) {
                System.out.print(".");
            }
            if (i % 100 == 0) {
                System.out.print(" ");
            }
            if (i % 500 == 0) {
                System.out.print("\n");
            }
        }
        return deprecatedUsages;
    }
}
