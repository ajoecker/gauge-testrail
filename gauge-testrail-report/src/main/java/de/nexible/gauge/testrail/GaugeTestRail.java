package de.nexible.gauge.testrail;

import com.google.common.base.Strings;
import de.nexible.gauge.testrail.config.GaugeDefaultContext;
import de.nexible.gauge.testrail.context.TestRailReportContext;
import de.nexible.gauge.testrail.context.TestRailReportDefaultContext;
import de.nexible.gauge.testrail.services.GitPRSpecFileNameDiffService;
import de.nexible.gauge.testrail.services.HashMapAuditArtifactModelStorageService;
import de.nexible.gauge.testrail.services.JsonAuditArtifactWriterService;
import de.nexible.gauge.testrail.services.GaugeScenarioInfoProxyService;

import java.io.IOException;
import java.util.logging.Logger;

import static de.nexible.gauge.testrail.config.GaugeTestRailLogger.initializeLogger;

/**
 * Accesspoint for the plugin.
 *
 * @author ajoecker
 */
public class GaugeTestRail {
    private static final HashMapAuditArtifactModelStorageService sharedAuditArtifactModelStorageService =
            new HashMapAuditArtifactModelStorageService();

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

        gaugeConnector.addGaugeResultListener(new TestRailDefaultHandler(testRailContext));
        gaugeConnector.addGaugeResultListener(
                new TestRailAuditArtifactHandler(
                    testRailContext,
                    new GitPRSpecFileNameDiffService(),
                    new JsonAuditArtifactWriterService(),
                    new GaugeScenarioInfoProxyService(),
                    sharedAuditArtifactModelStorageService));

        gaugeConnector.connect();
        gaugeConnector.listen();
    }
}