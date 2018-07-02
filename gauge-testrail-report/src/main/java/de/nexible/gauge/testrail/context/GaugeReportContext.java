package de.nexible.gauge.testrail.context;

import de.nexible.gauge.testrail.config.GaugeContext;

import java.nio.file.Path;

/**
 * A {@link GaugeReportContext} represents the bridge to Gauge relevant information.
 *
 * @author ajoecker
 */
public interface GaugeReportContext extends GaugeContext {
    /**
     * Whether the current run of the plugin is a rerun.
     *
     * @return
     */
    boolean isRerun();

    /**
     * Returns the path to the reports directory used by the plugin
     *
     * @return
     */
    Path getTestRailReportDir();
}
