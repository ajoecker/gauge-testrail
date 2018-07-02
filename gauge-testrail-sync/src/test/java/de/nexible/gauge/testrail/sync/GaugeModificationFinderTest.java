package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.gauge.GaugeConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GaugeModificationFinderTest {
    @Test
    @DisplayName("Single scenario with no tags is found")
    public void singleScenarioNoTags() throws IOException {
        List<Spec.ProtoSpec> list = new ArrayList<>();
        Spec.ProtoItem scenario = buildScenario("a scenario");
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(scenario).build();
        list.add(build);
        List<SpecModifications> modifications = getModifications(list);

        assertThat(modifications).hasSize(1);
    }

    @Test
    @DisplayName("Single scenario with  TestRail tags is not found")
    public void singleScenarioWithTestRailTags() throws IOException {
        List<Spec.ProtoSpec> list = new ArrayList<>();
        Spec.ProtoItem scenario = buildScenario("a scenario", "C123");
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(scenario).build();
        list.add(build);
        List<SpecModifications> modifications = getModifications(list);

        assertThat(modifications).isEmpty();
    }

    @Test
    @DisplayName("Single scenario with non TestRail tag is found")
    public void singleScenarioWithOtherTag() throws IOException {
        List<Spec.ProtoSpec> list = new ArrayList<>();
        Spec.ProtoItem scenario = buildScenario("a scenario", "smoke");
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(scenario).build();
        list.add(build);
        List<SpecModifications> modifications = getModifications(list);

        assertThat(modifications).hasSize(1);
    }

    @Test
    @DisplayName("Two scenarios with no tags / non TestTrail are found")
    public void twoScenariosWithNoTag() throws IOException {
        List<Spec.ProtoSpec> list = new ArrayList<>();
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario"))
                .addItems(buildScenario("another scenario", "smoke"))
                .build();
        list.add(build);
        List<SpecModifications> modifications = getModifications(list);
        assertThat(modifications).flatExtracting(SpecModifications::getHeadings).containsExactly("a scenario", "another scenario");
    }

    @Test
    @DisplayName("Two scenarios with no tags / TestTrail are found partially")
    public void twoScenariosWithNoAndTagsTag() throws IOException {
        List<Spec.ProtoSpec> list = new ArrayList<>();
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario"))
                .addItems(buildScenario("another scenario", "C23"))
                .build();
        list.add(build);
        List<SpecModifications> modifications = getModifications(list);
        assertThat(modifications).flatExtracting(SpecModifications::getHeadings).containsExactly("a scenario");
    }

    @Test
    @DisplayName("Two scenarios with estTrail are not found")
    public void twoScenariosWithTestRailTag() throws IOException {
        List<Spec.ProtoSpec> list = new ArrayList<>();
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario", "C23"))
                .addItems(buildScenario("another scenario", "C23"))
                .build();
        list.add(build);
        List<SpecModifications> modifications = getModifications(list);
        assertThat(modifications).isEmpty();
    }

    private Spec.ProtoItem buildScenario(String title, String... tags) {
        Spec.ProtoScenario.Builder ps = Spec.ProtoScenario.newBuilder().setScenarioHeading(title);
        if (tags != null) {
            for(String t : tags) {
                ps.addTags(t);
            }
        }
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Scenario).setScenario(ps.build()).build();
    }

    private List<SpecModifications> getModifications(List<Spec.ProtoSpec> list) throws IOException {
        GaugeConnector gc = Mockito.mock(GaugeConnector.class);
        Mockito.when(gc.getSpecs()).thenReturn(list);
        return new GaugeModificationFinder(gc).findModifications();
    }
}
