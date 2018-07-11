package de.nexible.gauge.testrail.sync;

import com.google.common.collect.ImmutableList;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.model.Tagged;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static de.nexible.gauge.testrail.sync.GaugeSpecRetrieval.retrieveSpecs;
import static org.assertj.core.api.Assertions.assertThat;

public class GaugeSpecRetrievalTest {
    @Test
    @DisplayName("Single scenario with no tags is found")
    public void singleScenarioNoTags() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(buildScenario("a scenario")).build();
        Tagged expected = Tagged.newInstance("a scenario", Optional.empty());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenariosWithTag).containsOnly(expected).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Single scenario with TestRail tags is not found")
    public void singleScenarioWithTestRailTags() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(buildScenario("a scenario", "C123")).build();
        Tagged expected = Tagged.newInstance("a scenario", Optional.of("C123"));
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenariosWithTag).containsOnly(expected).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Single scenario with non TestRail tag is found")
    public void singleScenarioWithOtherTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(buildScenario("a scenario", "smoke")).build();
        Tagged expected = Tagged.newInstance("a scenario", Optional.empty());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenariosWithTag).containsOnly(expected).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Two scenarios with no tags / non TestTrail are found")
    public void twoScenariosWithNoTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario"))
                .addItems(buildScenario("another scenario", "smoke"))
                .build();
        Tagged expected1 = Tagged.newInstance("a scenario", Optional.empty());
        Tagged expected2 = Tagged.newInstance("another scenario", Optional.empty());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenariosWithTag).containsOnly(expected1, expected2).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Two scenarios with no tags / TestTrail are found partially")
    public void twoScenariosWithNoAndTagsTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario"))
                .addItems(buildScenario("another scenario", "C23"))
                .build();
        Tagged expected1 = Tagged.newInstance("a scenario", Optional.empty());
        Tagged expected2 = Tagged.newInstance("another scenario", Optional.of("C23"));
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenariosWithTag).containsOnly(expected1, expected2).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Two scenarios with TestTrail are not found")
    public void twoScenariosWithTestRailTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario", "C23"))
                .addItems(buildScenario("another scenario", "C23"))
                .build();
        Tagged expected1 = Tagged.newInstance("a scenario", Optional.of("C23"));
        Tagged expected2 = Tagged.newInstance("another scenario", Optional.of("C23"));
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenariosWithTag).containsOnly(expected1, expected2).allMatch(p -> !p.hasBeenTagged());
    }

    private Spec.ProtoItem buildScenario(String title, String... tags) {
        Spec.ProtoScenario.Builder ps = Spec.ProtoScenario.newBuilder().setScenarioHeading(title);
        for (String t : tags) {
            ps.addTags(t);
        }
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Scenario).setScenario(ps.build()).build();
    }

}
