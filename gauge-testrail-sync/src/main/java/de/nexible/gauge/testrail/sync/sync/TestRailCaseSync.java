package de.nexible.gauge.testrail.sync.sync;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.config.TestRailUtil;
import de.nexible.gauge.testrail.sync.context.TestRailSyncContext;
import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.nexible.gauge.testrail.config.TestRailUtil.parseCaseId;
import static de.nexible.gauge.testrail.config.TestRailUtil.parseSectionId;

public class TestRailCaseSync implements Sync {
    private TestRailSyncContext testRailContext;

    public TestRailCaseSync(TestRailSyncContext testRailContext) {
        this.testRailContext = testRailContext;
    }

    @Override
    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        APIClient testRailClient = testRailContext.getTestRailClient();
        for (GaugeSpec gaugeSpec : specData) {
            gaugeSpec.getScenarios().stream().forEach(gaugeScenario -> sendToTestRail(testRailClient, gaugeSpec, gaugeScenario));
        }
        return specData;
    }

    private GaugeScenario sendToTestRail(APIClient testRailClient, GaugeSpec gaugeSpec, GaugeScenario scenario) {
        String sendTo = scenario.hasBeenTagged() ? addCase(parseSectionId(gaugeSpec.getTag())) : updateCase(parseCaseId(scenario.getTag()));
        System.out.println(sendTo);
        List<String> allSteps = new ArrayList<>(gaugeSpec.getSteps());
        allSteps.addAll(scenario.getSteps());
        System.out.println("ALL STEPS: " + allSteps);
        try {
            JSONObject postResult = (JSONObject) testRailClient.sendPost(sendTo, buildDataObject(allSteps, scenario.getHeading()));
            System.out.println("result: " + postResult);
            if (scenario.hasBeenTagged()) {
                scenario.setTag("C" + postResult.get("id"));
            }
        } catch (IOException | APIException e) {
            // TODO logger
            e.printStackTrace();
        }
        return scenario;
    }

    private JSONObject buildDataObject(List<String> scenario, String heading) {
        JSONObject data = new JSONObject();
        data.put("steps", TestRailUtil.formatSteps(scenario));
        data.put("template_id", testRailContext.getGaugeTemplateId());
        data.put("title", heading);
        data.put("custom_automation_type", "Gauge");
        System.out.println("data: " + data);
        return data;
    }

    private String updateCase(int caseId) {
        return "update_case/" + caseId;
    }

    private String addCase(int sectionId) {
        return "add_case/" + sectionId;
    }

    public static void main(String[] args) {
        APIClient client = new APIClient("https://joecker.testrail.io");
        client.setUser("ajoecker@yahoo.com");
        client.setPassword("NCFgI3wdyozwonG6WxL/-QMLq6PiFuUBU7hFciFUJ");
    }
}
