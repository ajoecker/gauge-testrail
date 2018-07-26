package de.nexible.gauge.testrail.sync;

import de.nexible.gauge.testrail.config.GaugeContext;
import de.nexible.gauge.testrail.config.GaugeDefaultContext;
import de.nexible.gauge.testrail.config.GaugeTestRailLogger;
import de.nexible.gauge.testrail.sync.context.TestRailSyncContext;
import de.nexible.gauge.testrail.sync.context.TestRailSyncDefaultContext;
import de.nexible.gauge.testrail.sync.gauge.GaugeConnector;
import de.nexible.gauge.testrail.sync.gauge.GaugeSpecRetriever;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.sync.SpecModifier;
import de.nexible.gauge.testrail.sync.sync.Sync;
import de.nexible.gauge.testrail.sync.sync.TestRailCaseSync;
import de.nexible.gauge.testrail.sync.sync.TestRailSectionSync;

import java.util.List;

import static de.nexible.gauge.testrail.sync.GaugeSpecRetrieval.retrieveSpecs;

public class TestRailSync implements Sync {
    private final TestRailSyncContext testRailContext;
    private final String gaugeProjectDir;

    public TestRailSync(TestRailSyncContext testRailContext, String gaugeProjectDir) {
        this.testRailContext = testRailContext;
        this.gaugeProjectDir = gaugeProjectDir;
    }

    public static void main(String[] args) {
        TestRailSyncContext testRailContext = new TestRailSyncDefaultContext();
        GaugeContext gaugeContext = new GaugeDefaultContext();
        GaugeTestRailLogger.initializeLogger(gaugeContext, "testrail-sync.log", testRailContext.getLogLevel());
        List<GaugeSpec> specs = getSpecs(testRailContext);
        new TestRailSync(testRailContext, gaugeContext.getGaugeProjectRoot()).sync(specs);
    }

    private static List<GaugeSpec> getSpecs(TestRailSyncContext testRailContext) {
        return retrieveSpecs(GaugeConnector.getSpecs(GaugeSpecRetriever::fetchAllSpecs, testRailContext.getGaugeApiPort()));
    }

    @Override
    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        List<GaugeSpec> specList = new TestRailSectionSync(testRailContext).sync(specData);
        specList = new TestRailCaseSync(testRailContext, gaugeProjectDir).sync(specList);
        return new SpecModifier(testRailContext).sync(specList);
    }
}
