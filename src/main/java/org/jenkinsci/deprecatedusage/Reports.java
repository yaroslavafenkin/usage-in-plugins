package org.jenkinsci.deprecatedusage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Reports {
    private final UpdateCenter updateCenter;
    private final DeprecatedApi deprecatedApi;
    private final Map<String, DeprecatedUsage> deprecatedUsageByPlugin;

    public Reports(UpdateCenter updateCenter, DeprecatedApi deprecatedApi,
            Map<String, DeprecatedUsage> deprecatedUsageByPlugin) {
        super();
        this.updateCenter = updateCenter;
        this.deprecatedApi = deprecatedApi;
        this.deprecatedUsageByPlugin = deprecatedUsageByPlugin;
    }

    public void report() {
        log("ignored deprecated api : " + format(DeprecatedApi.IGNORED_DEPRECATED_CLASSES));
        log("ignored plugins : " + DeprecatedUsage.IGNORED_PLUGINS);
        log("");
        log("<h3 id=deprecatedApi>Deprecated api in jenkins.war</h3>");
        log("<b> deprecated classes in jenkins.war : </b><pre>"
                + format(deprecatedApi.getClasses()) + "</pre>");
        println();
        log("<b> deprecated methods in jenkins.war : </b><pre>"
                + formatMethods(deprecatedApi.getMethods()) + "</pre>");
        println();
        log("<b> deprecated fields in jenkins.war : </b><pre>" + format(deprecatedApi.getFields())
                + "</pre>");
        println();

        final Set<String> deprecatedClassesUsed = new TreeSet<>();
        final Set<String> deprecatedMethodsUsed = new TreeSet<>();
        final Set<String> deprecatedFieldsUsed = new TreeSet<>();
        for (final DeprecatedUsage deprecatedUsage : deprecatedUsageByPlugin.values()) {
            deprecatedClassesUsed.addAll(deprecatedUsage.getClasses());
            deprecatedMethodsUsed.addAll(deprecatedUsage.getMethods());
            deprecatedFieldsUsed.addAll(deprecatedUsage.getFields());
        }
        logDeprecatedApiUsedInPlugins(deprecatedClassesUsed, deprecatedMethodsUsed,
                deprecatedFieldsUsed);

        final Set<String> deprecatedClassesNotUsed = filterOnJenkins(deprecatedApi.getClasses());
        final Set<String> deprecatedMethodsNotUsed = filterOnJenkins(deprecatedApi.getMethods());
        final Set<String> deprecatedFieldsNotUsed = filterOnJenkins(deprecatedApi.getFields());
        deprecatedClassesNotUsed.removeAll(deprecatedClassesUsed);
        deprecatedMethodsNotUsed.removeAll(deprecatedMethodsUsed);
        deprecatedFieldsNotUsed.removeAll(deprecatedFieldsUsed);
        log("<h3 id=deprecatedApiNotUsed>Deprecated api not used</h3>");
        log("<b> deprecated and public Jenkins classes not used in latest published plugins : </b><pre> "
                + format(deprecatedClassesNotUsed) + "</pre>");
        println();
        log("<b> deprecated and public Jenkins methods not used in latest published plugins : </b><pre> "
                + formatMethods(deprecatedMethodsNotUsed) + "</pre>");
        println();
        log("<b> deprecated and public Jenkins fields not used in latest published plugins : </b><pre> "
                + format(deprecatedFieldsNotUsed) + "</pre>");
        println();

        log("<h3 id=pluginsByDeprecatedApi>Plugins by deprecated api</h3>");
        logPluginsByDeprecatedApi(deprecatedClassesUsed, deprecatedMethodsUsed,
                deprecatedFieldsUsed);

        log("<h3 id=plugins>Plugins using a deprecated api</h3>");
        log(new TreeSet<>(deprecatedUsageByPlugin.keySet()).toString());
        println();

        log("<h3 id=summary>Summary</h3>");
        log(deprecatedApi.getClasses().size() + " deprecated and public classes in jenkins.war");
        log(deprecatedApi.getMethods().size() + " deprecated and public methods in jenkins.war");
        log(deprecatedApi.getFields().size() + " deprecated and public fields in jenkins.war");
        log(updateCenter.getPlugins().size() + " published plugins");
        log(deprecatedUsageByPlugin.size() + " plugins using a deprecated api");
        log(deprecatedClassesUsed.size() + " deprecated classes used in plugins");
        log(deprecatedMethodsUsed.size() + " deprecated methods used in plugins");
        log(deprecatedFieldsUsed.size() + " deprecated fields used in plugins");
        log(deprecatedClassesNotUsed.size()
                + " deprecated and public Jenkins classes not used in latest published plugins");
        log(deprecatedMethodsNotUsed.size()
                + " deprecated and public Jenkins methods not used in latest published plugins");
        log(deprecatedFieldsNotUsed.size()
                + " deprecated and public Jenkins fields not used in latest published plugins");
    }

    private void logDeprecatedApiUsedInPlugins(Set<String> deprecatedClassesUsed,
            Set<String> deprecatedMethodsUsed, Set<String> deprecatedFieldsUsed) {
        print("<h3 id=deprecatedApiByPlugins>Deprecated api used by plugins</h3>");
        println();
        for (final DeprecatedUsage deprecatedUsage : deprecatedUsageByPlugin.values()) {
            final String pluginWiki = getPluginWiki(deprecatedUsage.getPluginName());
            print("<h3 id=" + deprecatedUsage.getPluginName()
                    + "> deprecated api used in plugin <a href='" + pluginWiki + "'> ");
            println();
            print(deprecatedUsage.getPluginKey() + " </a>:</h3>");
            println();
            if (!deprecatedUsage.getClasses().isEmpty()) {
                log("   classes : " + format(deprecatedUsage.getClasses()));
            }
            if (!deprecatedUsage.getMethods().isEmpty()) {
                log("   methods : " + formatMethods(deprecatedUsage.getMethods()));
            }
            if (!deprecatedUsage.getFields().isEmpty()) {
                log("   fields : " + format(deprecatedUsage.getFields()));
            }
        }
        println();

        log("<h3 id=deprecatedApiUsed>Deprecated api used</h3>");
        log("<b> deprecated classes used in plugins : </b><pre> " + format(deprecatedClassesUsed)
                + "</pre>");
        println();
        log("<b> deprecated methods used in plugins : </b><pre> "
                + formatMethods(deprecatedMethodsUsed) + "</pre>");
        println();
        log("<b> deprecated fields used in plugins : </b><pre> " + format(deprecatedFieldsUsed)
                + "</pre>");
        println();
    }

    private void logPluginsByDeprecatedApi(Set<String> deprecatedClassesUsed,
            Set<String> deprecatedMethodsUsed, Set<String> deprecatedFieldsUsed) {
        for (final String deprecatedClass : deprecatedClassesUsed) {
            final List<String> plugins = new ArrayList<>();
            for (final DeprecatedUsage deprecatedUsage : deprecatedUsageByPlugin.values()) {
                if (deprecatedUsage.getClasses().contains(deprecatedClass)) {
                    plugins.add(deprecatedUsage.getPluginKey());
                }
            }
            log("plugins using deprecated <b> " + format(deprecatedClass) + " : </b>");
            log("   " + plugins);
        }
        for (final String deprecatedMethod : deprecatedMethodsUsed) {
            final List<String> plugins = new ArrayList<>();
            for (final DeprecatedUsage deprecatedUsage : deprecatedUsageByPlugin.values()) {
                if (deprecatedUsage.getMethods().contains(deprecatedMethod)) {
                    plugins.add(deprecatedUsage.getPluginKey());
                }
            }
            log("plugins using deprecated <b> " + formatMethods(deprecatedMethod) + " : </b>");
            log("   " + plugins);
        }
        for (final String deprecatedField : deprecatedFieldsUsed) {
            final List<String> plugins = new ArrayList<>();
            for (final DeprecatedUsage deprecatedUsage : deprecatedUsageByPlugin.values()) {
                if (deprecatedUsage.getFields().contains(deprecatedField)) {
                    plugins.add(deprecatedUsage.getPluginKey());
                }
            }
            log("plugins using deprecated <b> " + format(deprecatedField) + " : </b>");
            log("   " + plugins);
        }
        log("");
    }

    private static String formatMethods(Set<String> methods) {
        return formatMethods(methods.toString());
    }

    private static String formatMethods(String methods) {
        return format(methods.replace("java/lang/", "").replace(")V", ")").replace(")L", ") ")
                .replace("(L", "(").replace(";L", ";").replace(";)", ")").replace(".<init>", ""));
    }

    private static String format(Set<String> classesOrFieldsOrMethods) {
        return format(classesOrFieldsOrMethods.toString());
    }

    private static String format(String classOrFieldOrMethod) {
        // replace "org/mypackage/Myclass" by "org.mypackage.Myclass"
        return classOrFieldOrMethod.replace('/', '.');
    }

    private static Set<String> filterOnJenkins(Set<String> classesOrFieldsOrMethods) {
        final Set<String> filtered = new LinkedHashSet<>();
        for (final String classOrFieldOrMethod : classesOrFieldsOrMethods) {
            if (classOrFieldOrMethod.startsWith("jenkins/")
                    || classOrFieldOrMethod.startsWith("hudson/")
                    || classOrFieldOrMethod.startsWith("org/kohsuke/")) {
                filtered.add(classOrFieldOrMethod);
            }
        }
        return filtered;
    }

    private String getPluginWiki(String pluginName) {
        for (final JenkinsFile plugin : updateCenter.getPlugins()) {
            if (pluginName.equals(plugin.getName())) {
                return plugin.getWiki();
            }
        }
        return null;
    }

    private static void println() {
        print("\n");
    }

    private static void print(String message) {
        Log.print(message);
    }

    private static void log(String message) {
        Log.log(message);
    }
}
