package org.jenkinsci.deprecatedusage;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Command line options for usages scan.
 * Not thread safe.
 */
public class Options {

    private static final Options OPTIONS = new Options();
    private static List<String> additionalClasses;
    private static Map<String, Set<String>> additionalMethodNames;
    private static Map<String, Set<String>> additionalFields;

    @Option(name = "-h", aliases = "--help", usage = "Shows help")
    public boolean help;

    @Option(name = "-c", aliases = "--includeJavaCoreClasses", usage = "Include classes from java.* and javax.* in the report (not included by default)")
    public boolean includeJavaCoreClasses;

    @Option(name = "-C", aliases = "--additionalClasses", metaVar = "FILENAME", usage = "File name for additional classes to scan")
    public File additionalClassesFile;

    @Option(name = "--onlyAdditionalClasses", depends = "-C", usage = "Only include in the report the specified classes")
    public boolean onlyAdditionalClasses;

    @Option(name = "--onlyIncludeJenkinsClasses", usage = "Only include in the report Jenkins related classes (jenkins.*, hudson.*, etc.")
    public boolean onlyIncludeJenkinsClasses;

    @Option(name = "-u", aliases = "--updateCenter", usage = "Specifies update center URL to fetch plugins from")
    public URL updateCenterUrl;

    @Option(name = "-M", aliases = "--additionalMethods", metaVar = "FILENAME", usage = "File name for additional methods to scan")
    public File additionalMethodsFile;

    @Option(name = "--onlyAdditionalMethods", depends = "-M", usage = "Only include in the report the specified methods")
    public boolean onlyAdditionalMethods;

    @Option(name = "-F", aliases = "--additionalFields", metaVar = "FILENAME", usage = "File name for additional fields to scan")
    public File additionalFieldsFile;

    @Option(name = "--onlyAdditionalFields", depends = "-F", usage = "Only include in the report the specified fields")
    public boolean onlyAdditionalFields;

    private Options() {
    }

    /**
     * Singleton
     */
    public static Options get() {
        return OPTIONS;
    }

    /**
     * Returns the additional classes if the related {@link #additionalClassesFile} has been specified.
     *
     * @throws IllegalArgumentException if called when {@link #additionalClassesFile} has not been specified.
     */
    static List<String> getAdditionalClasses() throws IllegalArgumentException {
        if (additionalClasses != null) {
            return additionalClasses;
        }
        File additionalClassesFile = get().additionalClassesFile;
        if (!additionalClassesFile.exists()) {
            throw new IllegalArgumentException(
                    "Additional classes file option provided, but file cannot be found (" + additionalClassesFile + ")");
        }

        try {
            additionalClasses = FileUtils.readLines(additionalClassesFile, StandardCharsets.UTF_8.name());
            System.out.println(additionalClassesFile + " found, adding " + additionalClasses.size() + " classes");
            for (String additionalClass : additionalClasses) {
                System.out.println("\tadding " + additionalClass);
            }
            return additionalClasses;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static Map<String, Set<String>> getAdditionalMethodNames() {
        if (additionalMethodNames != null) {
            return additionalMethodNames;
        }
        Path path = get().additionalMethodsFile.toPath();
        if (Files.notExists(path)) {
            throw new IllegalArgumentException("Additional methods file option provided, but file not found: " + path);
        }
        additionalMethodNames = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                int hash = line.indexOf('#');
                if (hash == -1) {
                    continue;
                }
                String className = line.substring(0, hash).replaceAll("\\.", "/");
                String methodName = line.substring(hash + 1);
                additionalMethodNames.computeIfAbsent(className, ignored -> new LinkedHashSet<>()).add(methodName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return additionalMethodNames;
    }

    static Map<String, Set<String>> getAdditionalFields() {
        if (additionalFields != null) {
            return additionalFields;
        }
        Path path = get().additionalFieldsFile.toPath();
        if (Files.notExists(path)) {
            throw new IllegalArgumentException("Additional fields file option provided, but file not found: " + path);
        }
        additionalFields = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                int hash = line.indexOf('#');
                if (hash == -1) {
                    continue;
                }
                String className = line.substring(0, hash).replaceAll("\\.", "/");
                String methodName = line.substring(hash + 1);
                additionalFields.computeIfAbsent(className, ignored -> new LinkedHashSet<>()).add(methodName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return additionalFields;
    }
}
