package de.nexible.gauge.testrail.sync.sync;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.sync.context.TestRailSyncContext;
import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.nexible.gauge.testrail.config.TestRailUtil.parseCaseId;
import static de.nexible.gauge.testrail.config.TestRailUtil.parseSectionId;
import static de.nexible.gauge.testrail.sync.sync.CaseFormatter.getCaseText;

public class TestRailCaseSync implements Sync {
    private static final Logger logger = Logger.getLogger(TestRailCaseSync.class.getName());

    private TestRailSyncContext testRailContext;

    public TestRailCaseSync(TestRailSyncContext testRailContext) {
        this.testRailContext = testRailContext;
    }

    @Override
    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        logger.info(() -> "Sync test cases");
        APIClient testRailClient = testRailContext.getTestRailClient();
        for (GaugeSpec gaugeSpec : specData) {
            gaugeSpec.getScenarios().stream().forEach(gaugeScenario -> sendToTestRail(testRailClient, gaugeSpec, gaugeScenario));
        }
        return specData;
    }

    private GaugeScenario sendToTestRail(APIClient testRailClient, GaugeSpec spec, GaugeScenario scenario) {
        String sendTo = scenario.hasTag() ? updateCase(parseCaseId(scenario.getTag())) : addCase(parseSectionId(spec.getTag()));
        logger.info(() -> "Scenario '" + scenario.getHeading() + "' uses " + sendTo);
        List<String> allSteps = new ArrayList<>(spec.getSteps());
        allSteps.addAll(scenario.getSteps());
        try {
            String caseTag = getCaseTag(testRailClient, spec, scenario, sendTo);
            if (!scenario.hasTag()) {
                scenario.setTag(caseTag);
                logger.info(() -> "Scenario '" + scenario.getHeading() + "' now has tag " + scenario.getTag());
            }
        } catch (IOException | APIException e) {
            logger.log(Level.WARNING, e, () -> "Failed to update scenario '" + scenario.getHeading() + "'");
        }
        return scenario;
    }

    private String getCaseTag(APIClient testRailClient, GaugeSpec spec, GaugeScenario scenario, String sendTo) throws IOException, APIException {
        JSONObject data = buildDataObject(getCaseText(spec, scenario), scenario.getHeading());
        if (testRailContext.isDryRun()) {
            logger.info(() -> "Dry run, use artifical case tag");
            System.out.println("Would send " + data);
            return "C999";
        }
        JSONObject postResult = (JSONObject) testRailClient.sendPost(sendTo, data);
        return "C" + postResult.get("id");
    }

    private JSONObject buildDataObject(String text, String heading) {
        JSONObject data = new JSONObject();
        data.put("custom_steps", text);
        data.put("template_id", testRailContext.getTemplateId());
        data.put("title", heading);
        int automationId = testRailContext.getAutomationId();
        if (testRailContext.isKnown(automationId)) {
            data.put("custom_automation_type", automationId);
        }
        return data;
    }

    private String updateCase(int caseId) {
        return "update_case/" + caseId;
    }

    private String addCase(int sectionId) {
        return "add_case/" + sectionId;
    }
}
