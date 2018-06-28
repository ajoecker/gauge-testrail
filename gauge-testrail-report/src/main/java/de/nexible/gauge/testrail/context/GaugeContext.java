package de.nexible.gauge.testrail.context;

import java.nio.file.Path;

/**
 * A {@link GaugeContext} represents the bridge to Gauge relevant information.
 *
 * @author ajoecker
 */
public interface GaugeContext {
    /**
     * Whether the current run of the plugin is a rerun.
     *
     * @return
     */
    boolean isRerun();

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

    /**
     * Returns the path to the reports directory used by the plugin
     *
     * @return
     */
    Path getTestRailReportDir();
}
