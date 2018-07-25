package de.nexible.gauge.testrail;

import de.nexible.gauge.testrail.config.GaugeContext;
import de.nexible.gauge.testrail.config.GaugeDefaultContext;
import de.nexible.gauge.testrail.config.GaugeTestRailLogger;
import de.nexible.gauge.testrail.context.TestRailReportContext;
import de.nexible.gauge.testrail.context.TestRailReportDefaultContext;

import java.io.IOException;

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
        GaugeConnector gaugeConnector = new GaugeConnector();
        gaugeConnector.addGaugeResultListener(new TestRailDefaultHandler(testRailContext));
        gaugeConnector.connect();
        gaugeConnector.listen();
    }
}