package de.nexible.gauge.testrail.sync.sync;

import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.sync.context.TestRailSyncContext;
import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.nexible.gauge.testrail.config.TestRailUtil.parseCaseId;
import static de.nexible.gauge.testrail.config.TestRailUtil.parseSectionId;

public class TestRailCaseSync implements Sync {
    private static final Logger logger = Logger.getLogger(TestRailCaseSync.class.getName());

    private final TestRailSyncContext testRailContext;
    private final CaseFormatter caseFormatter;

    public TestRailCaseSync(TestRailSyncContext testRailContext, CaseFormatter caseFormatter) {
        this.testRailContext = testRailContext;
        this.caseFormatter = caseFormatter;
    }

    @Override
    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        logger.info(() -> "Sync test cases");
        for (GaugeSpec gaugeSpec : specData) {
            gaugeSpec.getScenarios().stream().forEach(gaugeScenario -> sendToTestRail(gaugeSpec, gaugeScenario));
        }
        return specData;
    }

    private GaugeScenario sendToTestRail(GaugeSpec spec, GaugeScenario scenario) {
        try {
            String caseTag = getCaseTag(spec, scenario);
            if (!scenario.hasTag()) {
                scenario.setTag(caseTag);
                logger.info(() -> "Scenario '" + scenario.getHeading() + "' now has tag " + scenario.getTag());
            }
        } catch (IOException | APIException e) {
            logger.log(Level.WARNING, e, () -> "Failed to update scenario '" + scenario.getHeading() + "'");
        }
        return scenario;
    }

    private String getCaseTag(GaugeSpec spec, GaugeScenario scenario) throws IOException, APIException {
        JSONObject data = buildDataObject(spec, scenario);
        if (testRailContext.isDryRun()) {
            logger.info(() -> "Dry run, use artifical case tag");
            logger.info(() -> "Would send " + data);
            return "C999";
        }
        if (scenario.hasTag()) {
            return testRailContext.getTestRailClient().updateCase(parseCaseId(scenario.getTag()), data);
        }
        return testRailContext.getTestRailClient().addCase(parseSectionId(spec.getTag()), data);
    }

    private JSONObject buildDataObject(GaugeSpec spec, GaugeScenario scenario) {
        JSONObject data = new JSONObject();
        data.put("custom_steps", caseFormatter.getCaseText(spec, scenario));
        data.put("template_id", testRailContext.getTemplateId());
        data.put("title", scenario.getHeading());
        int automationId = testRailContext.getAutomationId();
        if (testRailContext.isKnown(automationId)) {
            data.put("custom_automation_type", automationId);
        }
        caseFormatter.getScenarioLocation(spec, scenario).ifPresent(location -> data.put("custom_scenario_location", location));

        return data;
    }
}
