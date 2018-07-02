package de.nexible.gauge.testrail.sync;

import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.config.GaugeDefaultContext;
import de.nexible.gauge.testrail.sync.context.TestRailSyncDefaultContext;
import de.nexible.gauge.testrail.sync.gauge.GaugeConnector;
import de.nexible.gauge.testrail.sync.gauge.GaugeSpecRetriever;

import java.io.IOException;
import java.util.List;

public class TestRailSync {
    private final SpecModifier specModifier;
    private TestRailConnector testRailConnector;
    private GaugeModificationFinder gaugeModificationFinder;

    public TestRailSync(SpecModifier specModifier, TestRailConnector testRailConnector, GaugeModificationFinder gaugeModificationFinder) {
        this.specModifier = specModifier;
        this.testRailConnector = testRailConnector;
        this.gaugeModificationFinder = gaugeModificationFinder;
    }

    public static void main(String[] args) throws IOException, APIException {
        TestRailSyncDefaultContext testRailContext = new TestRailSyncDefaultContext();
        TestRailConnector testRailConnector = new TestRailConnector(testRailContext, new GaugeDefaultContext());
        GaugeConnector gaugeConnector = new GaugeConnector(new GaugeSpecRetriever(), testRailContext);
        new TestRailSync(new SpecModifier(), testRailConnector, new GaugeModificationFinder(gaugeConnector)).start();
    }

    private void start() throws IOException, APIException {
        List<SpecModifications> modifications = gaugeModificationFinder.findModifications();
        testRailConnector.upload(modifications);
        specModifier.persistChanges(modifications);
    }
}
