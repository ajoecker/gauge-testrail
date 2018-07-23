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
Call `gauge install testrail` to install a released version of the plugin.

To install a custom build call `gauge install testrail -f <path>/testrail-<version>.zip`

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
The plugin creates the log file `testrail.log` in the `logs` folder of the gauge project. Remember, the log file is overwritten with each run of the plugin

### Rerun
If in any case the posting to TestRail failed, it is possible to re-start the plugin without the need to re-build the gauge test result.
The plugin creates a file called `last_run.json` inside the `reports/testrail` directory along with the properties that were used for the last run and an executable file.
To re-run the plugin and to post the test results from the last run to TestRail, one can simply call `./testrail-recovery`
