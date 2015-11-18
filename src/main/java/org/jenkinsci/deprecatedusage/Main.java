package org.jenkinsci.deprecatedusage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        final long start = System.currentTimeMillis();
        log("** Finds and reports usage of deprecated Jenkins api in plugins (except api used in jelly and groovy files) **");
        final UpdateCenter updateCenter = new UpdateCenter();
        log("Downloaded update-center.json");
        updateCenter.download();
        log("All files are up to date (" + updateCenter.getPlugins().size() + " plugins)");

        log("Analyzing deprecated api in Jenkins");
        final File coreFile = updateCenter.getCore().getFile();
        final DeprecatedApi deprecatedApi = new DeprecatedApi();
        deprecatedApi.analyze(coreFile);

        Log.print("Analyzing deprecated usage in plugins ");
        int i = 0;
        final Map<String, DeprecatedUsage> deprecatedUsageByPlugin = new HashMap<>();
        for (final JenkinsFile plugin : updateCenter.getPlugins()) {
            final DeprecatedUsage deprecatedUsage = new DeprecatedUsage(deprecatedApi);
            deprecatedUsage.analyze(plugin.getFile());
            if (deprecatedUsage.hasDeprecatedUsage()) {
                final String pluginKey = plugin.getName() + '-' + plugin.getVersion();
                deprecatedUsageByPlugin.put(pluginKey, deprecatedUsage);
            }
            i++;
            if (i % 20 == 0) {
                Log.print(".");
            }
        }
        log("");

        new Reports(deprecatedApi, deprecatedUsageByPlugin).report();

        log("duration : " + (System.currentTimeMillis() - start) + " ms");
    }

    private static void log(String message) {
        Log.log(message);
    }
}
