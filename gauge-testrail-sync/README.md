# Gauge Testrail report

This projects is plugin for [gauge](http://getgauge.io) to sync gauge test cases with [TestRail](http://http://www.gurock.com/testrail/)

## Getting started

### Pre-requisite

- [Install Gauge](https://docs.gauge.org/installing.html#installation)
- [Java 8+](https://www.java.com/en/download/index.jsp)
- [Maven](https://maven.apache.org/install.html)

### Compile plugin
Call `mvn clean install` in the root directory

### Build plugin
To build the plugin, call `mvn clean package`.
This will build the assembly file `artifacts/testrail-sync-<version>.zip`, which can be installed as gauge plugin

### Install plugin
Call `gauge install testrail-sync -f <path>/testrail-sync-<version>.zip`

### Use plugin
In the gauge project make sure to add `testrail-sync` as plugin in the `manifest.json`, as also create the `testrails.properties` file under `env/default`.

The properties file must contain the following entries:
```
testrail.user = // the user to login. must be an email known to TestRail
testrail.token = // the token of the user (see http://docs.gurock.com/testrail-api2/accessing)
testrail.url = // the base url of the TestRail instance
testrail.gauge.template.id = // the id of the gauge template (see section "TestRail configuration")
testrail.gauge.spec.label = // the name of the spec file custom field in TestRail (see section "TestRail configuration")
testrail.section = // the id of the section where the cases shall be added
testrail.gauge.link = // link prefix to the spec file (e.g git link to the project, up to the project root)
```

Call `gauge docs testrail-sync`

### Uninstall plugin
Call `gauge uninstall testrail-sync` and remove the entry from the `manifest.json` of the gauge project

### Concept
The plugin inspects all scenarios in the project and checks, whether the scenarios have a TestRail tag (`Cxxx`).
If not, the test case is uploaded to TestRail and the spec file is changed with the newly created TestRail case id added.

__Attention__ if the gauge project, the plugin is running on, is under version control, any changes made by the plugin must be committed manually !

### Important notice
The specifications and scenarios __must__ be written with the `#` or `##`, respectively !

### TestRail configuration
the plugin assumes, that a template is configured in TestRail. This template can be any template, as long as it does not require any fields to be set,
except the title
If in the template a fields is configured as `Link`, to point to the spec file, the property `testrail.gauge.spec.label` must be defined, which contains
the database name of that label. Besides that, `testrail.gauge.link` must be set, that contains the prefix of the spec file link (e.g. github link to the
gauge project)

### Todo
* Adding reference option to upload to TestRail
* Better section handling than in properties file, as different test cases might end up in different sections
