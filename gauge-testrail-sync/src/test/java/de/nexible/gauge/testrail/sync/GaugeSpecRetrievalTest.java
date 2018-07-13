package de.nexible.gauge.testrail.sync;

import com.google.common.collect.ImmutableList;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static de.nexible.gauge.testrail.sync.GaugeSpecRetrieval.retrieveSpecs;
import static org.assertj.core.api.Assertions.assertThat;

public class GaugeSpecRetrievalTest {
    @Test
    @DisplayName("Single scenario with no tags is found")
    public void singleScenarioNoTags() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(buildScenario("a scenario")).build();
        GaugeScenario expected = GaugeScenario.newInstance(buildScenario("a scenario").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Single scenario with TestRail tags is not found")
    public void singleScenarioWithTestRailTags() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(buildScenario("a scenario", "C123")).build();
        GaugeScenario expected = GaugeScenario.newInstance(buildScenario("a scenario", "C123").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Single scenario with non TestRail tag is found")
    public void singleScenarioWithOtherTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(buildScenario("a scenario", "smoke")).build();
        GaugeScenario expected = GaugeScenario.newInstance(buildScenario("a scenario", "smoke").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Two scenarios with no tags / non TestTrail are found")
    public void twoScenariosWithNoTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario"))
                .addItems(buildScenario("another scenario", "smoke"))
                .build();
        GaugeScenario expected1 = GaugeScenario.newInstance(buildScenario("a scenario").getScenario());
        GaugeScenario expected2 = GaugeScenario.newInstance(buildScenario("another scenario").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected1, expected2).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Two scenarios with no tags / TestTrail are found partially")
    public void twoScenariosWithNoAndTagsTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario"))
                .addItems(buildScenario("another scenario", "C23"))
                .build();
        GaugeScenario expected1 = GaugeScenario.newInstance(buildScenario("a scenario").getScenario());
        GaugeScenario expected2 = GaugeScenario.newInstance(buildScenario("another scenario", "C23").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected1, expected2).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Two scenarios with TestTrail are not found")
    public void twoScenariosWithTestRailTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario", "C23"))
                .addItems(buildScenario("another scenario", "C23"))
                .build();
        GaugeScenario expected1 = GaugeScenario.newInstance(buildScenario("a scenario", "C23").getScenario());
        GaugeScenario expected2 = GaugeScenario.newInstance(buildScenario("another scenario", "C23").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected1, expected2).allMatch(p -> !p.hasBeenTagged());
    }

    private Spec.ProtoItem buildScenario(String title, String... tags) {
        Spec.ProtoScenario.Builder ps = Spec.ProtoScenario.newBuilder().setScenarioHeading(title);
        for (String t : tags) {
            ps.addTags(t);
        }
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Scenario).setScenario(ps.build()).build();
    }

}
