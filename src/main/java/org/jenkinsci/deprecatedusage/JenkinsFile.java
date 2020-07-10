package org.jenkinsci.deprecatedusage;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.DigestException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class JenkinsFile {
    private final String name;
    private final String version;
    private final String url;
    private final String wiki;
    private Path file;
    private final Checksum checksum;

    public JenkinsFile(String name, String version, String url, String wiki, Checksum checksum) {
        super();
        this.name = name;
        this.version = version;
        this.url = url;
        this.wiki = wiki;
        this.checksum = checksum;
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        file = Paths.get("work", name, version, fileName).toAbsolutePath();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getWiki() {
        return wiki;
    }

    public File getFile() {
        return file.toFile();
    }

    public void setFile(File file) {
        this.file = file.toPath();
    }

    public void deleteFile() throws IOException {
        Files.delete(file);
    }

    public CompletableFuture<Void> downloadIfNotExists(CloseableHttpAsyncClient client) {
        if (Files.exists(file)) {
            return CompletableFuture.completedFuture(null);
        }
        return download(client);
    }

    private CompletableFuture<Void> download(CloseableHttpAsyncClient client) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            future.completeExceptionally(e);
            return future;
        }
        SimpleHttpRequest request = SimpleHttpRequests.get(url);
        class Callback implements FutureCallback<SimpleHttpResponse> {
            // TODO: figure out how to re-use the configured HttpRequestRetryStrategy instead of this workaround
            //  (see http request/response/exec chain interceptors potentially?)
            private int retriesRemaining = 3;

            @Override
            public void completed(SimpleHttpResponse result) {
                try {
                    byte[] data = result.getBodyBytes();
                    if (checksum != null && !checksum.matches(data)) {
                        if (retriesRemaining > 0) {
                            System.out.println("Retrying download of " + url + " due to invalid message digest");
                            retriesRemaining--;
                            client.execute(request, this);
                        } else {
                            future.completeExceptionally(new DigestException(url));
                        }
                    } else {
                        Files.write(file, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        System.out.printf("Downloaded %s @ %.2f kiB%n", url, (data.length / 1024.0));
                        future.complete(null);
                    }
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void failed(Exception ex) {
                future.completeExceptionally(ex);
            }

            @Override
            public void cancelled() {
                System.out.println("Download cancelled for " + url);
                future.cancel(true);
            }
        }
        client.execute(request, new Callback());
        return future;
    }

    @Override
    public String toString() {
        return url + " -> " + file.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JenkinsFile that = (JenkinsFile) o;
        return Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
