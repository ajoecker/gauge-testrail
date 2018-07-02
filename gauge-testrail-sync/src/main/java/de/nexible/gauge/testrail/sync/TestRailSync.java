package de.nexible.gauge.testrail.sync;

import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.config.GaugeDefaultContext;
import de.nexible.gauge.testrail.sync.gauge.GaugeConnector;
import de.nexible.gauge.testrail.sync.gauge.GaugeSpecRetriever;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestRailSync {
    private static final Pattern TESTRAIL_PATTERN = Pattern.compile("^[Cc]\\d+$");
    private final SpecModifier specModifier;
    private GaugeConnector gaugeConnector;
    private TestRailConnector testRailConnector;

    public TestRailSync(SpecModifier specModifier, GaugeConnector gaugeConnector, TestRailConnector testRailConnector) {
        this.specModifier = specModifier;
        this.gaugeConnector = gaugeConnector;
        this.testRailConnector = testRailConnector;
    }

    public static void main(String[] args) throws IOException, APIException {
        TestRailConnector testRailConnector = new TestRailConnector(new TestRailSyncDefaultContext(), new GaugeDefaultContext());
        GaugeConnector gaugeConnector = new GaugeConnector(new GaugeSpecRetriever());
        new TestRailSync(new SpecModifier(), gaugeConnector, testRailConnector).start();
    }

    private void start() throws IOException, APIException {
        List<SpecModification> modifications = sync(gaugeConnector.connect());
        testRailConnector.upload(modifications);
        specModifier.persistChanges(modifications);
    }

    private List<SpecModification> sync(List<Spec.ProtoSpec> protoSpecList) {
        return protoSpecList.stream().map(this::syncSpec).collect(Collectors.toList());
    }

    private SpecModification syncSpec(Spec.ProtoSpec spec) {
        return spec.getItemsList().stream()
                .filter(item -> item.getItemType() == Spec.ProtoItem.ItemType.Scenario)
                .map(item -> item.getScenario())
                .filter(scenario -> scenario.getTagsList().stream().filter(this::isTestRailTag).findAny().isPresent())
                .filter(Objects::nonNull)
                .collect(() -> new SpecModification(spec.getFileName()), SpecModification::add, (x, y) -> {
                });
    }

    private boolean isTestRailTag(String s) {
        return TESTRAIL_PATTERN.matcher(s).find();
    }

}
