# Gauge Testrail report

This projects is plugin for [gauge](http://getgauge.io) to post gauge test results into [TestRail](http://http://www.gurock.com/testrail/)

## Getting started

### Pre-requisite

- [Install Gauge](https://docs.gauge.org/installing.html#installation)
- [Java 8+](https://www.java.com/en/download/index.jsp)
- [Maven](https://maven.apache.org/install.html)

### Compile plugin
Call `mvn clean install` in the root directory

### Build plugin
To build the plugin, call `mvn clean package`.
This will build the assembly file `artifacts/testrail-<version>.zip`, which can be installed as gauge plugin

### Install plugin
Call `gauge install testrail -f <path>/testrail-<version>.zip`

### Use plugin
In the gauge project make sure to add `testrail` as plugin in the `manifest.json`, as also create the `testrails.properties` file under `env/default`.
The properties file must contain the following entries:
```
testrail.user = // the user to login. must be an email known to TestRail
testrail.token = // the token of the user (see http://docs.gurock.com/testrail-api2/accessing)
testrail.url = // the base url of the TestRail instance
testrail.run.id = // the id of the testrun to post the results to
```

### Uninstall plugin
Call `gauge uninstall testrail` and remove the entry from the `manifest.json` of the gauge project

### Concept
The plugin reports test results from a Gauge test run to TestRail.

To be able to do so, all gauge test cases (scenarios) must be tagged with the corresponding TestRail case id.

For example like
```
## an example scenario
tags: C234, any other tag
* step 1
* step 2
```
The TestRail case ids follow the pattern `C\d+`.
Any scenario, that do not have any TestRail case id tagged to it, will be ignored by the plugin. However, a scenario can have multiple TestRail case ids.
In this case, the scenario result is posted individually for each of the tagged TestRail case ids.

### Logs
The plugin creates the log file `testrails.log` in the `logs` folder of the gauge project. Remember, the log file is overwritten with each run of the plugin

### ToDos
- Rerun possibility in case of an issue while reporting to TestRail. This means to make sure that the Gauge results can be reused,
when something went wrong during posting the results to TestRail to avoid running the whole gauge test suite again
