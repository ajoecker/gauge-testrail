package de.nexible.gauge.testrail;

import com.thoughtworks.gauge.Spec;

import java.util.Arrays;

class SpecBuildHelper {
    private SpecBuildHelper() {
        // static
    }


    static Spec.ProtoSuiteResult suiteResult(Spec.ProtoScenario scenario) {
        Spec.ProtoItem item = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Scenario).setScenario(scenario).build();
        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().setSpecHeading("spec heading").setFileName("path/to/file").addItems(item).build();
        Spec.ProtoSpecResult specResult = Spec.ProtoSpecResult.newBuilder().setProtoSpec(spec).setExecutionTime(1234).setScenarioCount(1).build();
        return Spec.ProtoSuiteResult.newBuilder().setExecutionTime(1234)
                .addSpecResults(specResult).setSuccessRate(100).setEnvironment("default").setProjectName("projectName").setTimestamp("Jun 27, 2018 at 10:28am").build();

    }

    static Spec.ProtoSuiteResult suiteResult() {
        Spec.ProtoScenario testTag = scenario("testTag");
        return suiteResult(testTag);
    }

    static Spec.ProtoScenario scenario(String... tag) {
        Spec.ProtoScenario.Builder scenario = Spec.ProtoScenario.newBuilder();
        Spec.ProtoStep step = Spec.ProtoStep.newBuilder().setActualText("This is a simple step").build();
        Spec.ProtoItem scenarioItem = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(step).build();
        scenario.setScenarioHeading("a simple test").addScenarioItems(scenarioItem).setExecutionStatusValue(1).setExecutionTime(1234);

        if (tag != null) {
            Arrays.stream(tag).forEach(scenario::addTags);
        }

        return scenario.build();
    }

}
