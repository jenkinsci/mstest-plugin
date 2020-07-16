# Change Log

### Version 0.20 (September 1st, 2017)

* The release description is available on github: https://github.com/jenkinsci/mstest-plugin/releases/tag/mstest-0.20

### Version 0.19 (September 1st, 2015)

* Support for web tests (contacted by email, by Peter Barnes. No Jira issue has been opened)
* Let the users still using Java 1.6 to continue using the plugin [[JENKINS-29032]](https://issues.jenkins-ci.org/browse/JENKINS-29032)
* Mark the inconclusive tests as 'skipped' [[JENKINS-29316]](https://issues.jenkins-ci.org/browse/JENKINS-29316)

### Version 0.18 (May 12th, 2015) --- !!! Java 1.7 is required !!!

* Add support for "Retain long standard output/error" [[JENKINS-28281]](https://issues.jenkins-ci.org/browse/JENKINS-28281). 
The default value for this option is false. 
If you're automating the creation of your jobs, simply specify keepLongStdio=on as a parameter of your query. 
Any other value than 'on' will set this option to false.
* Add localized messages for it, pt-BR, fr
* Cumulated code coverage filename: vstest.coveragexml
* Add default values for test result pattern and "fail if no result file is found": **/*.trx and true.

### Version 0.17 (May 4th, 2015) --- !!! Java 1.7 is required !!!

* Add a checkbox to ignore missing TRX files (Thanks Christopher Bush, pull request [#7](https://github.com/jenkinsci/mstest-plugin/pull/7)). 
The pull request contains also a way to automate job creation using the REST API. 
So, if you're automating the creation of your jobs, just specify failOnError=on to enable this feature. 
Any other value than 'on' will set this option to false.
* Fix the code coverage calculations (Thanks junshanxu, pull request
[#6](https://github.com/jenkinsci/mstest-plugin/pull/6)): a sum over all the nodes is better than using the value of the first node only.

### Version 0.16 (Apr 14th, 2015) --- !!! Java 1.7 is required !!!

* Show the code coverage graph for coveragexml files (one of the two XSD, the one produced by vstest)

### Version 0.15 (Apr 14th, 2015) --- !!! Java 1.7 is required !!!

* Improve support for data driven tests (Thanks, Darryl Melander: pull request [#6](https://github.com/jenkinsci/mstest-plugin/pull/5))
* Preserve charsets while fixing TRX files (JENKINS-23531, reopened by JitinJohn@MS)

### Version 0.14 (Apr 1st, 2015)

* Support for output/stdout messages (JENKINS-19384)
* Drop invalid XML entities (JENKINS-23531). MSTest allows writing XML entities corresponding to invalid XML characters. 
These XML entities generate exceptions while being parsed by Java parsers. 
For me, it's still unclear if such entities are standard or not. 
However, to avoid these exceptions, the mstest parser simply drops them. 
These entities normally correspond to non printable characters.
* Support for .coveragexml files. 
The coverage data present in these files is being transformed in an EMMA coverage report. 
Today, you can try to generate vscoveragexml files using
https://github.com/gredman/CI.MSBuild.Tasks or
https://github.com/yasu-s/CoverageConverter.

### Version 0.13 (Mar 18, 2015)

* Support for ignored tests (JENKINS_27469)
* Support for data driven tests (JENKINS-8193, JENKINS-4075)
* Support for timed out tests (JENKINS-11332)
* Support for TextMessages (JENKINS-17506)
* Improved processing for tests whose @outcome is not set
* Stacktraces are now shown as stacktraces, and error messages as error
messages

### Version 0.12 (Mar 12, 2015)

* Convert MS XML code coverage reports in emma coverage reports, and show them.
* Fix: the tests for which the outcome is 'error' (or missing, with an error message or a stack trace) 
will be reported as junit errors.

### Version 0.11 (Jan 17, 2015)

* Support vstest TRX format
* Support environment variables as target (vstestrunner-plugin exports the full path to the TRX as environment variable)

### Version 0.7 (Jun 17, 2011)

* Supported MSTest 2010 ordered tests ([JENKINS-7458](https://issues.jenkins-ci.org/browse/JENKINS-7458))
* Supported wildcard ([JENKINS-8520](https://issues.jenkins-ci.org/browse/JENKINS-8520))

### Version 0.6 (Feb 11, 2010)

* Fixed issue [JENKINS-3906](https://issues.jenkins-ci.org/browse/JENKINS-3906):
Durations greater than 59s
* Fixed issue [JENKINS-4632](https://issues.jenkins-ci.org/browse/JENKINS-4632): 
MSTest plugin does not parse Visual Studio 2010 results

### Version 0.5 (Feb 6, 2010)

* Update code for more recent Hudson

### Version 0.4 (Jun 16, 2009)

* Fixed the _AbortException_ issue
* Added i18n support
* Added Brazilian portuguese localization

### Version 0.3

* Indentifies test's class using the ExecutionId variable

### Version 0.2

* Fixed a problem to identify namespace and class name from the TestMethod tag
* Changed JUnit test report file name

### Version 0.1

* Initial Release
