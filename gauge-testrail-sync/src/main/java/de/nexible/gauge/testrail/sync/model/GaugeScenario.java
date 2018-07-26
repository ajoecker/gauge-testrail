package de.nexible.gauge.testrail.sync.model;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.config.TestRailUtil;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static de.nexible.gauge.testrail.sync.GaugeSpecRetrieval.extractComments;
import static de.nexible.gauge.testrail.sync.GaugeSpecRetrieval.extractSteps;

public class GaugeScenario extends Tagged {
    private long scenarioLocation = 0;

    public static GaugeScenario newInstance(Spec.ProtoScenario scenario) {
        GaugeScenario tagged = new GaugeScenario();
        tagged.heading = scenario.getScenarioHeading();
        tagged.tag = findTestRailTag(scenario.getTagsList(), TestRailUtil::isTestRailTag);
        tagged.setComments(extractComments(scenario.getScenarioItemsList()));
        tagged.setSteps(extractSteps(newArrayList(concat(scenario.getScenarioItemsList(), scenario.getTearDownStepsList()))));
        tagged.scenarioLocation = scenario.getSpan().getStart();
        return tagged;
    }

    public long getScenarioLocation() {
        return scenarioLocation;
    }
}
