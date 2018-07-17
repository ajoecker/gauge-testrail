package de.nexible.gauge.testrail.sync;

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
    private TestRailSyncContext testRailContext;

    public TestRailSync(TestRailSyncContext testRailContext) {
        this.testRailContext = testRailContext;
    }

    public static void main(String[] args) {
        TestRailSyncContext testRailContext = new TestRailSyncDefaultContext();
        GaugeTestRailLogger.initializeLogger(new GaugeDefaultContext(), "testrail-sync.log");
        List<GaugeSpec> specs = getSpecs(testRailContext);
        new TestRailSync(testRailContext).sync(specs);
    }

    private static List<GaugeSpec> getSpecs(TestRailSyncContext testRailContext) {
        return retrieveSpecs(GaugeConnector.getSpecs(GaugeSpecRetriever::fetchAllSpecs, testRailContext.getGaugeApiPort()));
    }

    @Override
    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        List<GaugeSpec> specList = new TestRailSectionSync(testRailContext).sync(specData);
        specList = new TestRailCaseSync(testRailContext).sync(specList);
        return new SpecModifier().sync(specList);
    }
}
