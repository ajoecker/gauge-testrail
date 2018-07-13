package de.nexible.gauge.testrail.sync.model;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.config.TestRailUtil;

import java.util.ArrayList;

public class GaugeScenario extends Tagged {
    public static GaugeScenario newInstance(Spec.ProtoScenario scenario) {
        GaugeScenario tagged = new GaugeScenario();
        tagged.heading = scenario.getScenarioHeading();
        tagged.tag = scenario
                .getTagsList()
                .stream()
                .filter(TestRailUtil::isTestRailTag)
                .findAny();
        ArrayList<Spec.ProtoItem> items = new ArrayList<>(scenario.getScenarioItemsList());
        items.addAll(scenario.getTearDownStepsList());
        tagged.setSteps(items);
        return tagged;
    }
}
