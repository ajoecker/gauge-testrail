package de.nexible.gauge.testrail.config;

import static java.lang.System.getenv;

public class GaugeDefaultContext implements GaugeContext {
    public String getGaugeProjectRoot() {
        return getenv("GAUGE_PROJECT_ROOT");
    }

    public String getGaugeLogDir() {
        return System.getenv("logs_directory");
    }
}
