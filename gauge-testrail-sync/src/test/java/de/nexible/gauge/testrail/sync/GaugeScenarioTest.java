package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class GaugeScenarioTest {
    @Test
    @DisplayName("A scenario with one step and no tag is parsed correctly")
    public void scenarioOneStepNoTag() {
        Spec.ProtoStep theStep1 = Spec.ProtoStep.newBuilder().setParsedText("step1").build();
        Spec.ProtoItem step1 = Spec.ProtoItem.newBuilder().setStep(theStep1).setItemType(Spec.ProtoItem.ItemType.Step).build();
        Spec.ProtoScenario scenario = Spec.ProtoScenario.newBuilder().setScenarioHeading("a scenario").addScenarioItems(step1).build();
        GaugeScenario gaugeScenario = GaugeScenario.newInstance(scenario);

        assertAll("scenario",
                () -> assertThat(gaugeScenario.getSteps()).containsOnly("step1"),
                () ->assertThat(gaugeScenario.hasTag()).isFalse(),
                () -> assertThat(gaugeScenario.getHeading()).isEqualTo("a scenario"));
    }

    @Test
    @DisplayName("A scenario with one step and TestRail tag is parsed correctly")
    public void scenarioOneStepTestRailTag() {
        Spec.ProtoStep theStep1 = Spec.ProtoStep.newBuilder().setParsedText("step1").build();
        Spec.ProtoItem step1 = Spec.ProtoItem.newBuilder().setStep(theStep1).setItemType(Spec.ProtoItem.ItemType.Step).build();
        Spec.ProtoScenario scenario = Spec.ProtoScenario.newBuilder().setScenarioHeading("a scenario").addTags("C234").addScenarioItems(step1).build();
        GaugeScenario gaugeScenario = GaugeScenario.newInstance(scenario);

        assertAll("scenario",
                () -> assertThat(gaugeScenario.getSteps()).containsOnly("step1"),
                () ->assertThat(gaugeScenario.hasTag()).isTrue(),
                () -> assertThat(gaugeScenario.getHeading()).isEqualTo("a scenario"));
    }

    @Test
    @DisplayName("A scenario with one step and tag is parsed correctly")
    public void scenarioOneStepTag() {
        Spec.ProtoStep theStep1 = Spec.ProtoStep.newBuilder().setParsedText("step1").build();
        Spec.ProtoItem step1 = Spec.ProtoItem.newBuilder().setStep(theStep1).setItemType(Spec.ProtoItem.ItemType.Step).build();
        Spec.ProtoScenario scenario = Spec.ProtoScenario.newBuilder().setScenarioHeading("a scenario").addTags("smoke").addScenarioItems(step1).build();
        GaugeScenario gaugeScenario = GaugeScenario.newInstance(scenario);

        assertAll("scenario",
                () -> assertThat(gaugeScenario.getSteps()).containsOnly("step1"),
                () ->assertThat(gaugeScenario.hasTag()).isFalse(),
                () -> assertThat(gaugeScenario.getHeading()).isEqualTo("a scenario"));
    }

    @Test
    @DisplayName("A scenario with one step, a teardown step and tag is parsed correctly")
    public void scenarioOneStepTagWithTeardown() {
        Spec.ProtoStep theStep1 = Spec.ProtoStep.newBuilder().setParsedText("step1").build();
        Spec.ProtoItem step1 = Spec.ProtoItem.newBuilder().setStep(theStep1).setItemType(Spec.ProtoItem.ItemType.Step).build();
        Spec.ProtoStep teardownStep = Spec.ProtoStep.newBuilder().setParsedText("teardown").build();
        Spec.ProtoItem teardown = Spec.ProtoItem.newBuilder().setStep(teardownStep).setItemType(Spec.ProtoItem.ItemType.Step).build();;
        Spec.ProtoScenario scenario = Spec.ProtoScenario.newBuilder().setScenarioHeading("a scenario").addTags("smoke").addScenarioItems(step1).addTearDownSteps(teardown).build();
        GaugeScenario gaugeScenario = GaugeScenario.newInstance(scenario);

        assertAll("scenario",
                () -> assertThat(gaugeScenario.getSteps()).containsOnly("step1"),
                () ->assertThat(gaugeScenario.hasTag()).isFalse(),
                () -> assertThat(gaugeScenario.getHeading()).isEqualTo("a scenario"));
    }
}
