# Gauge Testrail

This projects provides plugins or utilities for combining [TestRail](http://http://www.gurock.com/testrail/) and [gauge](http://getgauge.io)

## Projects
### gauge-testrail-report
Gauge plugin, that posts Gauge test results to a test run in TestRail.

See project documentation for more information

### gauge-testrail-sync
Gauge plugin, to synchronize gauge test cases with TestRail.

See project documentation for more information

### TestRailAuditArtifactHandler

This handler for the GaugeResultListener attempts to run a diff vs master, and generates a build artifact that reports
unannotated scenarios, so that development and quality can work to create matching test cases in Testrail.

The code is decently self explanatory with a couple of exceptions:

I'm not sure if `String file = scenarioInfo.getDescriptorForType().getFile().getName();` is legitimate - 
with that in mind, `IS_FILE_PATH_FOR_SCENARIOS_TOTALLY_BOGUS` is a boolean that you can set to true (default: `false`) to explicitly not run
 the diff.
 
#### `TestRailAuditArtifactHandler#UNANNOTATED_TEST_ARTIFACT_PATH`

A string constant you will want to change to reflect where you want to save the artifact. 
