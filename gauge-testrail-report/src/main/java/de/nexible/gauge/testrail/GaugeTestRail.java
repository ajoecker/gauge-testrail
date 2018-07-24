package de.nexible.gauge.testrail;

import de.nexible.gauge.testrail.config.GaugeContext;
import de.nexible.gauge.testrail.config.GaugeDefaultContext;
import de.nexible.gauge.testrail.config.GaugeTestRailLogger;
import de.nexible.gauge.testrail.context.TestRailReportContext;
import de.nexible.gauge.testrail.context.TestRailReportDefaultContext;

import java.io.IOException;

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
        GaugeContext gaugeContext = new GaugeDefaultContext();
        TestRailReportContext testRailContext = new TestRailReportDefaultContext();
        GaugeTestRailLogger.initializeLogger(gaugeContext, "testrail.log", testRailContext.getLogLevel());

        GaugeResultListener testRailHandler = new TestRailDefaultHandler(testRailContext);

        GaugeConnector gaugeConnector = new GaugeConnector();
        gaugeConnector.addGaugeResultListener(testRailHandler);
        gaugeConnector.connect();
        gaugeConnector.listen();
    }
}