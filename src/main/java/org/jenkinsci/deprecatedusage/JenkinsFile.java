package org.jenkinsci.deprecatedusage;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JenkinsFile {
    private final String name;
    private final String version;
    private final String url;
    private final String wiki;
    private final Path versionsRootDirectory;
    private Path file;
    private final Checksum checksum;

    public JenkinsFile(String name, String version, String workDir, String url, String wiki, Checksum checksum) {
        super();
        this.name = name;
        this.version = version;
        this.url = url;
        this.wiki = wiki;
        this.checksum = checksum;
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        versionsRootDirectory = Paths.get("work", workDir, name);
        file = versionsRootDirectory.resolve(version).resolve(fileName);
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

    public CompletableFuture<Void> downloadIfNotExists(CloseableHttpAsyncClient client, Executor executor) {
        if (Files.exists(file)) {
            return CompletableFuture.supplyAsync(() -> {
                if (checksum == null) {
                    return false;
                }
                try {
                    return checksum.matches(file);
                } catch (IOException e) {
                    System.out.println("Error validating checksum for " + file);
                    System.out.println(e.toString());
                    return false;
                }
            }, executor).thenCompose(checksumOk -> checksumOk ? CompletableFuture.completedFuture(null) : download(client));
        }
        return download(client);
    }

    private CompletableFuture<Void> download(CloseableHttpAsyncClient client) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            if (Files.exists(versionsRootDirectory)) {
                Files.walkFileTree(versionsRootDirectory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            future.completeExceptionally(e);
            return future;
        }
        client.execute(SimpleHttpRequests.get(url), new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse result) {
                try {
                    byte[] data = result.getBodyBytes();
                    if (checksum == null || checksum.matches(data)) {
                        Files.write(file, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } else {
                        future.completeExceptionally(new DigestException("Invalid checksum for downloaded file " + file));
                        return;
                    }
                    System.out.printf("Downloaded %s @ %.2f kiB%n", url, (data.length / 1024.0));
                    future.complete(null);
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
                System.out.println("Download cancelled for " + file);
                future.cancel(true);
            }
        });
        return future;
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
