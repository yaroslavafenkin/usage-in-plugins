# Tool to scan for API Usage in Jenkins plugins

This tools offers a facility to scan Jenkins plugins for a given API usage.
It will automatically download all existing Jenkins plugins, and analyze them using the provided criteria.
 
NOTE: it will **NOT** find usages in Jelly and Groovy files, and in WEB-INF/lib/*.jar.


[![Build Status](https://ci.jenkins-ci.org/buildStatus/icon?job=Reporting/infra_deprecated-usage-in-plugins)](https://ci.jenkins-ci.org/view/All/job/Reporting/job/infra_deprecated-usage-in-plugins/)

See details and deprecated usage for each plugin in the [continuous integration](https://ci.jenkins-ci.org/view/All/job/Reporting/job/infra_deprecated-usage-in-plugins/lastSuccessfulBuild/artifact/target/output.html) or in this [example of output](../../blob/master/Output_example.html).

## See also:
* [Jenkins policy for API deprecation](https://issues.jenkins-ci.org/browse/JENKINS-31035)
* The old [Plugin compat tester](https://github.com/jenkinsci/plugin-compat-tester) and its [disabled job](https://ci.jenkins-ci.org/job/plugin-compat-tester/)

## Usage

To run the tool yourself, see [USAGE](USAGE.adoc)

### Historical note

This tool was originally designed to look exclusively for @Deprecated code usages.
In early 2019, this has been extended to allow looking for any class usage.

## References

Creator: Emeric Vernat

## LICENSE

[License MIT](../../blob/master/LICENSE.txt)
