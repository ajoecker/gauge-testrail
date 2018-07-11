package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;

import java.util.List;
import java.util.stream.Collectors;

public class GaugeSpecRetrieval {
    public static List<GaugeSpec> retrieveSpecs(List<Spec.ProtoSpec> protoSpecList) {
        return protoSpecList.stream().map(GaugeSpecRetrieval::getSpecs).collect(Collectors.toList());
    }

    private static GaugeSpec getSpecs(Spec.ProtoSpec spec) {
        return spec.getItemsList().stream()
                .filter(item -> item.getItemType() == Spec.ProtoItem.ItemType.Scenario)
                .map(item -> item.getScenario())
                .collect(() -> GaugeSpec.newInstance(spec), GaugeSpec::addScenario, (x, y) -> {
                });
    }
}
