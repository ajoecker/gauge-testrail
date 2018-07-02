package de.nexible.gauge.testrail.config;

public interface GaugeContext {

    /**
     * Returns the root directory of the gauge project the plugin was run from
     *
     * @return
     */
    String getGaugeProjectRoot();

    /**
     * Returns the name of the report directory
     *
     * @return
     */
    String getGaugeReportsDir();

    /**
     * Returns the name of the log directory
     *
     * @return
     */
    String getGaugeLogDir();
}
