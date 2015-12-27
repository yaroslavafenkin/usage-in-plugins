package org.jenkinsci.deprecatedusage;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipException;

public class Main {
    private static final String UPDATE_CENTER_URL =
    // "http://updates.jenkins-ci.org/experimental/update-center.json";
    "http://updates.jenkins-ci.org/update-center.json";

    public static void main(String[] args) throws Exception {
        final long start = System.currentTimeMillis();
        log("<h2> Finds and reports usage of deprecated Jenkins api in plugins </h2> (except api used in jelly and groovy files and in WEB-INF/lib/*.jar)");
        final UpdateCenter updateCenter = new UpdateCenter(new URL(UPDATE_CENTER_URL));
        log("Downloaded update-center.json");
        updateCenter.download();
        log("All files are up to date (" + updateCenter.getPlugins().size() + " plugins)");

        log("Analyzing deprecated api in Jenkins");
        final File coreFile = updateCenter.getCore().getFile();
        final DeprecatedApi deprecatedApi = new DeprecatedApi();
        deprecatedApi.analyze(coreFile);

        Log.log("Analyzing deprecated usage in plugins");
        final Map<String, DeprecatedUsage> deprecatedUsageByPlugin = analyzeDeprecatedUsage(
                updateCenter.getPlugins(), deprecatedApi);

        new Reports(updateCenter, deprecatedApi, deprecatedUsageByPlugin).report();

        log("duration : " + (System.currentTimeMillis() - start) + " ms at "
                + DateFormat.getDateTimeInstance().format(new Date()));
        Log.closeLog();
    }

    private static Map<String, DeprecatedUsage> analyzeDeprecatedUsage(List<JenkinsFile> plugins,
            final DeprecatedApi deprecatedApi) throws InterruptedException, ExecutionException {
        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors());
        final List<Future<DeprecatedUsage>> futures = new ArrayList<>(plugins.size());
        for (final JenkinsFile plugin : plugins) {
            final Callable<DeprecatedUsage> task = new Callable<DeprecatedUsage>() {
                @Override
                public DeprecatedUsage call() throws IOException {
                    final DeprecatedUsage deprecatedUsage = new DeprecatedUsage(plugin.getName(),
                            plugin.getVersion(), deprecatedApi);
                    try {
                        deprecatedUsage.analyze(plugin.getFile());
                    } catch (final EOFException e) {
                        Log.log("deleting " + plugin.getFile().getName()
                                + " and skipping, because " + e.toString());
                        plugin.getFile().delete();
                    } catch (final ZipException e) {
                        Log.log("deleting " + plugin.getFile().getName()
                                + " and skipping, because " + e.toString());
                        plugin.getFile().delete();
                    } catch (final Exception e) {
                        Log.log(e.toString() + " on " + plugin.getFile().getName());
                    }
                    return deprecatedUsage;
                }
            };
            futures.add(executorService.submit(task));
        }

        final Map<String, DeprecatedUsage> deprecatedUsageByPlugin = new LinkedHashMap<>();
        int i = 0;
        for (final Future<DeprecatedUsage> future : futures) {
            final DeprecatedUsage deprecatedUsage = future.get();
            if (deprecatedUsage.hasDeprecatedUsage()) {
                deprecatedUsageByPlugin.put(deprecatedUsage.getPluginKey(), deprecatedUsage);
            }
            i++;
            if (i % 20 == 0) {
                Log.print(".");
            }
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        // wait for threads to stop
        Thread.sleep(100);
        log("");
        log("");
        return deprecatedUsageByPlugin;
    }

    private static void log(String message) {
        Log.log(message);
    }
}
