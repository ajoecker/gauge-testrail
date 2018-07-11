package de.nexible.gauge.testrail.sync.model;

import com.google.common.collect.ImmutableList;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.config.TestRailConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GaugeSpec extends Tagged {
    private Path specFile;
    private List<Tagged> scenarios = new ArrayList<>();

    private GaugeSpec() {
        // static constructor
    }

    public static GaugeSpec newInstance(Spec.ProtoSpec spec) {
        GaugeSpec gaugeSpec = new GaugeSpec();
        gaugeSpec.specFile = Paths.get(spec.getFileName());
        gaugeSpec.tag = spec.getTagsList().stream().filter(TestRailConfig::isSectionTag).findAny();
        gaugeSpec.heading = spec.getSpecHeading();
        return gaugeSpec;
    }

    public Tagged addScenario(Spec.ProtoScenario scenario) {
        Optional<String> testRailTag = scenario
                .getTagsList()
                .stream()
                .filter(TestRailConfig::isTestRailTag)
                .findAny();
        Tagged tagged = newInstance(scenario.getScenarioHeading(), testRailTag);
        scenarios.add(tagged);
        return tagged;
    }

    public List<Tagged> getScenariosWithTag() {
        return ImmutableList.copyOf(scenarios);
    }

    public Path getSpecFile() {
        return specFile;
    }

    public Tagged getTaggedByName(String line) {
        String onlyHeading = line.substring(3).trim();
        return scenarios.stream().filter(t -> t.getHeading().equals(onlyHeading)).findFirst().orElse(newInstance("", Optional.empty()));
    }

    @Override
    public String toString() {
        return "GaugeSpecRetrieval{" +
                "specFile=" + specFile +
                ", scenarios=" + scenarios +
                '}';
    }
}
