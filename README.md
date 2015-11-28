**Finds and reports usage of deprecated Jenkins api in plugins** (except api used in jelly and groovy files and in WEB-INF/lib/*.jar)

[![Build Status](https://ci.jenkins-ci.org/buildStatus/icon?job=infra_deprecated-usage-in-plugins)](https://ci.jenkins-ci.org/view/Infrastructure/job/infra_deprecated-usage-in-plugins/)

Current results in summary:
* 1124 plugins
* 425 plugins using a deprecated Jenkins api
* 18 deprecated classes, 133 deprecated methods and 12 deprecated fields are used in plugins
* 29 deprecated and public Jenkins classes, 345 deprecated methods, 58 deprecated fields are not used in the latest published plugins

See details and deprecated usage for each plugin in the [continuous integration](https://ci.jenkins-ci.org/view/Infrastructure/job/infra_deprecated-usage-in-plugins/ws/target/output.html) or in this [example of output](https://github.com/evernat/deprecated-usage-in-plugins/blob/master/Output_example.txt).

See also:
* [Jenkins policy for API deprecation](https://issues.jenkins-ci.org/browse/JENKINS-31035)
* The old [Plugin compat tester](https://github.com/jenkinsci/plugin-compat-tester) and its [disabled job](https://ci.jenkins-ci.org/job/plugin-compat-tester/)

To run the tool yourself : Checkout and run with "mvn clean compile exec:java".
Note: it is quite long to download all the plugins the first time (1.8 GB).

[License MIT](https://github.com/evernat/deprecated-usage-in-plugins/blob/master/LICENSE.txt)

Author Emeric Vernat
