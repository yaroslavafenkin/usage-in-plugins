package org.jenkinsci.deprecatedusage;

import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Command line options for usages scan.
 * Not thread safe.
 */
public class Options {

    private static final Options OPTIONS = new Options();
    private static List<String> additionalClasses;

    @Option(name = "-h", aliases = "--help", usage = "Shows help")
    public boolean help;

    @Option(name = "-c", aliases = "--includeJavaCoreClasses", usage = "Include classes from java.* and javax.* in the report (not included by default)")
    public boolean includeJavaCoreClasses;

    @Option(name = "--additionalClasses", metaVar = "FILENAME", usage = "File name for additional classes to scan")
    public File additionalClassesFile;

    @Option(name = "--onlyAdditionalClasses", depends = "--additionalClasses", usage = "Only include in the report the specified classes")
    public boolean onlyAdditionalClasses;

    @Option(name = "--onlyIncludeJenkinsClasses", usage = "Only include in the report Jenkins related classes (jenkins.*, hudson.*, etc.")
    public boolean onlyIncludeJenkinsClasses;

    @Option(name = "-u", aliases = "--update-center", usage = "Specifies update center URL to fetch plugins from")
    public URL updateCenterUrl;

    private Options() {
    }

    /**
     * Singleton
     */
    public static final Options get() {
        return OPTIONS;
    }

    /**
     * Returns the additional classes if the related {@link #additionalClassesFile} has been specified.
     *
     * @throws IOException if called when {@link #additionalClassesFile} has not been specified.
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
}
