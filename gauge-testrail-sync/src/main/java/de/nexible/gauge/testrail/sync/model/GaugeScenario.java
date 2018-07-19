package de.nexible.gauge.testrail.sync.model;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.config.TestRailUtil;

import java.util.Optional;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;

public class GaugeScenario extends Tagged {
    public static GaugeScenario newInstance(Spec.ProtoScenario scenario) {
        GaugeScenario tagged = new GaugeScenario();
        tagged.heading = scenario.getScenarioHeading();
        tagged.tag = findTestRailTag(scenario.getTagsList(), TestRailUtil::isTestRailTag);
        tagged.setComments(scenario.getScenarioItemsList());
        tagged.setSteps(newArrayList(concat(scenario.getScenarioItemsList(), scenario.getTearDownStepsList())));
        return tagged;
    }
}
