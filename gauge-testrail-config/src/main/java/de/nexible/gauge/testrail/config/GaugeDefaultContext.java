package de.nexible.gauge.testrail.config;

public class GaugeDefaultContext implements GaugeContext {
    public String getGaugeProjectRoot() {
        return Environment.get("GAUGE_PROJECT_ROOT");
    }

    public String getGaugeLogDir() {
        return Environment.get("logs_directory");
    }
}
