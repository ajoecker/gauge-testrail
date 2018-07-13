package de.nexible.gauge.testrail.sync;

import de.nexible.gauge.testrail.config.GaugeDefaultContext;
import de.nexible.gauge.testrail.sync.context.TestRailSyncContext;
import de.nexible.gauge.testrail.sync.context.TestRailSyncDefaultContext;
import de.nexible.gauge.testrail.sync.gauge.GaugeConnector;
import de.nexible.gauge.testrail.sync.gauge.GaugeSpecRetriever;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.sync.SpecModifier;
import de.nexible.gauge.testrail.sync.sync.Sync;
import de.nexible.gauge.testrail.sync.sync.TestRailCaseSync;
import de.nexible.gauge.testrail.sync.sync.TestRailSectionSync;

import java.util.Arrays;
import java.util.List;

import static de.nexible.gauge.testrail.sync.GaugeSpecRetrieval.retrieveSpecs;

public class TestRailSync implements Sync {
    private final List<Sync> syncs;

    public TestRailSync(List<Sync> syncs) {
        this.syncs = syncs;
    }

    public static void main(String[] args) {
        TestRailSyncContext testRailContext = new TestRailSyncDefaultContext();
        List<GaugeSpec> specs = getSpecs(testRailContext);
        new TestRailSync(syncs(testRailContext)).sync(specs);
    }

    private static List<GaugeSpec> getSpecs(TestRailSyncContext testRailContext) {
        return retrieveSpecs(GaugeConnector.getSpecs(GaugeSpecRetriever::fetchAllSpecs, testRailContext.getGaugeApiPort()));
    }

    private static List<Sync> syncs(TestRailSyncContext testRailContext) {
        TestRailSectionSync testRailSectionSync = new TestRailSectionSync(testRailContext);
        TestRailCaseSync testRailCaseSync = new TestRailCaseSync(testRailContext);
        SpecModifier specModifier = new SpecModifier();
        return Arrays.asList(testRailSectionSync, testRailCaseSync, specModifier);
    }

    @Override
    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        for (Sync sync : syncs) {
            specData = sync.sync(specData);
        }
        return specData;
    }
}
