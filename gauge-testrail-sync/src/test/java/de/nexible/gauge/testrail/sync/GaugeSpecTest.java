package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class GaugeSpecTest {
    @Test
    public void simpleSpecWithNoTagNoScenarios() {
        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().setSpecHeading("a spec").setFileName("specFile").build();
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec);

        assertAll("gaugespec",
                () -> assertThat(gaugeSpec.getSpecFile()).isEqualTo(Paths.get("specFile")),
                () -> assertThat(gaugeSpec.getHeading()).isEqualTo("a spec"),
                () -> assertThat(gaugeSpec.hasTag()).isFalse(),
                () -> assertThat(gaugeSpec.getSteps()).isEmpty());
    }

    @Test
    public void simpleSpecWithSectionTagNoScenarios() {
        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().setSpecHeading("a spec").setFileName("specFile").addTags("section_123").build();
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec);

        assertAll("gaugespec",
                () -> assertThat(gaugeSpec.getSpecFile()).isEqualTo(Paths.get("specFile")),
                () -> assertThat(gaugeSpec.getHeading()).isEqualTo("a spec"),
                () -> assertThat(gaugeSpec.hasTag()).isTrue(),
                () -> assertThat(gaugeSpec.getSteps()).isEmpty());
    }

    @Test
    public void simpleSpecWithNoSectionTagNoScenarios() {
        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().setSpecHeading("a spec").setFileName("specFile").addTags("somehting").build();
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec);

        assertAll("gaugespec",
                () -> assertThat(gaugeSpec.getSpecFile()).isEqualTo(Paths.get("specFile")),
                () -> assertThat(gaugeSpec.getHeading()).isEqualTo("a spec"),
                () -> assertThat(gaugeSpec.hasTag()).isFalse(),
                () -> assertThat(gaugeSpec.getSteps()).isEmpty());
    }

    @Test
    public void simpleSpecWithNoSteps() {
        Spec.ProtoStep step = Spec.ProtoStep.newBuilder().setActualText("a step").build();
        Spec.ProtoItem stepItem = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(step).build();
        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().setSpecHeading("a spec").setFileName("specFile").addItems(stepItem).build();
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec);

        assertAll("gaugespec",
                () -> assertThat(gaugeSpec.getSpecFile()).isEqualTo(Paths.get("specFile")),
                () -> assertThat(gaugeSpec.getHeading()).isEqualTo("a spec"),
                () -> assertThat(gaugeSpec.hasTag()).isFalse(),
                () -> assertThat(gaugeSpec.getSteps()).containsOnly("a step"));
    }

    @Test
    public void findsScenarioWhenAvailable() {
        Spec.ProtoStep step = Spec.ProtoStep.newBuilder().setParsedText("a step").build();
        Spec.ProtoItem stepItem = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(step).build();
        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().setSpecHeading("a spec").setFileName("specFile").addItems(stepItem).build();
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec);

        Spec.ProtoStep scenarioStep = Spec.ProtoStep.newBuilder().setParsedText("scenario step").build();
        Spec.ProtoItem scenarioStepItem = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(scenarioStep).build();
        Spec.ProtoScenario scenario = Spec.ProtoScenario.newBuilder().addScenarioItems(scenarioStepItem).setScenarioHeading("a scenario").build();

        gaugeSpec.addScenario(scenario);

        Optional<GaugeScenario> gaugeScenario = gaugeSpec.findScenarioByName("## a scenario");
        assertThat(gaugeScenario.isPresent()).isTrue();
    }

    @Test
    public void findsNotScenarioWhenNonAvailable() {
        Spec.ProtoStep step = Spec.ProtoStep.newBuilder().setParsedText("a step").build();
        Spec.ProtoItem stepItem = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(step).build();
        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().setSpecHeading("a spec").setFileName("specFile").addItems(stepItem).build();
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec);

        Spec.ProtoStep scenarioStep = Spec.ProtoStep.newBuilder().setParsedText("scenario step").build();
        Spec.ProtoItem scenarioStepItem = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(scenarioStep).build();
        Spec.ProtoScenario scenario = Spec.ProtoScenario.newBuilder().addScenarioItems(scenarioStepItem).setScenarioHeading("a scenario").build();

        gaugeSpec.addScenario(scenario);

        Optional<GaugeScenario> gaugeScenario = gaugeSpec.findScenarioByName("## something");
        assertThat(gaugeScenario.isPresent()).isFalse();
    }
}
