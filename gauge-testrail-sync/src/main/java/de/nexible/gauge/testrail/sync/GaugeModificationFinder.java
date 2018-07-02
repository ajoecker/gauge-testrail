package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.config.TestRailConfig;
import de.nexible.gauge.testrail.sync.gauge.GaugeConnector;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GaugeModificationFinder {
    private GaugeConnector gaugeConnector;

    public GaugeModificationFinder(GaugeConnector gaugeConnector) {
        this.gaugeConnector = gaugeConnector;
    }

    public List<SpecModifications> findModifications() throws IOException {
        return gaugeConnector.getSpecs().stream().map(this::getSpecModifications).filter(SpecModifications::hasModifications).collect(Collectors.toList());
    }

    private SpecModifications getSpecModifications(Spec.ProtoSpec spec) {
        return spec.getItemsList().stream()
                .filter(item -> item.getItemType() == Spec.ProtoItem.ItemType.Scenario)
                .map(item -> item.getScenario())
                .filter(scenario -> !scenario.getTagsList().stream().filter(TestRailConfig::isTestRailTag).findAny().isPresent())
                .collect(() -> new SpecModifications(spec.getFileName()), SpecModifications::add, (x, y) -> {
                });
    }
}
