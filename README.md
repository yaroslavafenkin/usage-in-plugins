**Finds and reports usage of deprecated Jenkins api in plugins** (except api used in jelly and groovy files and in WEB-INF/lib/*.jar)

Checkout and run with "mvn exec:java".

Note: it is quite long to download all the plugins the first time (1.8 GB).

Current results in summary:
* 1125 plugins
* 435 plugins using a deprecated Jenkins api
* 19 deprecated classes, 166 deprecated methods and 12 deprecated fields are used in plugins
* 29 deprecated and public Jenkins classes, 312 deprecated methods, 58 deprecated fields are not used in the latest published plugins

See details and deprecated usage for each plugin in this [example of output](https://github.com/evernat/deprecated-usage-in-plugins/blob/master/Output_example.txt).

See also:
* [Jenkins policy for API deprecation](https://issues.jenkins-ci.org/browse/JENKINS-31035)
* The old [Plugin compat tester](https://github.com/jenkinsci/plugin-compat-tester) and its [disabled job](https://ci.jenkins-ci.org/job/plugin-compat-tester/)

[License MIT](https://github.com/evernat/deprecated-usage-in-plugins/blob/master/LICENSE.txt)

Author Emeric Vernat
