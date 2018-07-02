package de.nexible.gauge.testrail.sync;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.config.GaugeContext;
import de.nexible.gauge.testrail.sync.context.TestRailSynContext;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class TestRailConnector {
    private TestRailSynContext testRailContext;
    private GaugeContext gaugeContext;

    public TestRailConnector(TestRailSynContext testRailContext, GaugeContext gaugeContext) {
        this.testRailContext = testRailContext;
        this.gaugeContext = gaugeContext;
    }

    public void upload(List<SpecModifications> modifications) throws IOException, APIException {
        APIClient testRailClient = testRailContext.getTestRailClient();

        for (SpecModifications specModifications : modifications) {
            Collection<String> scenarioModifications = specModifications.getHeadings();
            for (String mod : scenarioModifications) {
                JSONObject result = send(testRailClient, createJSONObject(specModifications, mod));
                specModifications.setTag(mod, "C" + result.get("id"));
            }
        }
    }

    private JSONObject send(APIClient testRailClient, JSONObject obj) throws IOException, APIException {
        return (JSONObject) testRailClient.sendPost("add_case/" + testRailContext.getSectionId(), obj);
    }

    private JSONObject createJSONObject(SpecModifications specModifications, String mod) {
        JSONObject obj = new JSONObject();
        obj.put("title", mod);
        obj.put("template_id", testRailContext.getGaugeTemplateId());
        String specLink = testRailContext.getSpecLink();
        if (!"".equals(specLink)) {
            Path specFile = specModifications.getSpecFile();
            Path projectRoot = Paths.get(gaugeContext.getGaugeProjectRoot());
            String total = specLink + projectRoot.relativize(specFile);
            obj.put("custom_" + testRailContext.getSpecFieldLabel(), total);
        }
        return obj;
    }
}
