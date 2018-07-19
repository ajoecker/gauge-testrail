package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static de.nexible.gauge.testrail.sync.GaugeSpecRetrieval.retrieveSpecs;

public class TestRailSync implements Sync {
    private TestRailSyncContext testRailContext;

    public TestRailSync(TestRailSyncContext testRailContext) {
        this.testRailContext = testRailContext;
    }

    public static void main(String[] args) throws IOException {
        TestRailSyncContext testRailContext = new TestRailSyncDefaultContext();
        GaugeTestRailLogger.initializeLogger(new GaugeDefaultContext(), "testrail-sync.log");
        List<GaugeSpec> specs = getSpecs(testRailContext);

        //new TestRailSync(testRailContext).sync(specs);
    }

    private static List<GaugeSpec> getSpecs(TestRailSyncContext testRailContext) throws IOException {
        List<Spec.ProtoSpec> specs = GaugeConnector.getSpecs(GaugeSpecRetriever::fetchAllSpecs, testRailContext.getGaugeApiPort());
        Path path = Paths.get("spec-serialised.spec");
        try (OutputStream os = Files.newOutputStream(path)) {
            specs.get(0).writeTo(os);
        }
        return retrieveSpecs(specs);
    }

    @Override
    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        List<GaugeSpec> specList = new TestRailSectionSync(testRailContext).sync(specData);
        specList = new TestRailCaseSync(testRailContext).sync(specList);
        return new SpecModifier().sync(specList);
    }
}
