package de.nexible.gauge.testrail;

import com.google.common.base.Strings;
import de.nexible.gauge.testrail.config.GaugeContext;
import de.nexible.gauge.testrail.config.GaugeDefaultContext;
import de.nexible.gauge.testrail.config.GaugeTestRailLogger;
import de.nexible.gauge.testrail.context.TestRailReportContext;
import de.nexible.gauge.testrail.context.TestRailReportDefaultContext;

import java.io.IOException;
import java.util.logging.Logger;

import static de.nexible.gauge.testrail.config.GaugeTestRailLogger.initializeLogger;

/**
 * Accesspoint for the plugin.
 *
 * @author ajoecker
 */
public class GaugeTestRail {
    public static void main(String[] args) throws IOException {
        new GaugeTestRail().run();
    }

    private void run() throws IOException {
        TestRailReportContext testRailContext = new TestRailReportDefaultContext();
        initializeLogger(new GaugeDefaultContext(), "testrail.log", testRailContext.getLogLevel());
        if (Strings.isNullOrEmpty(testRailContext.getTestRailRunId())) {
            Logger.getLogger(GaugeTestRail.class.getName()).info(() -> "TestRail plugin is disabled - no report send");
        }
        else {
            Logger.getLogger(GaugeTestRail.class.getName()).info(() -> "TestRail plugin is runs for " + testRailContext.getTestRailRunId());
        }
        GaugeConnector gaugeConnector = new GaugeConnector();
        gaugeConnector.addGaugeResultListener(new TestRailAuditArtifactHandler(testRailContext));
        gaugeConnector.connect();
        gaugeConnector.listen();
    }
}