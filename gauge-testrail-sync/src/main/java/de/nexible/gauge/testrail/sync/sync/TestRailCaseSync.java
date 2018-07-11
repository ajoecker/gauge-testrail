package de.nexible.gauge.testrail.sync.sync;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.config.GaugeContext;
import de.nexible.gauge.testrail.sync.context.TestRailSyncContext;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.model.Tagged;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestRailCaseSync implements Sync {
    private TestRailSyncContext testRailContext;
    private GaugeContext gaugeContext;

    public TestRailCaseSync(TestRailSyncContext testRailContext, GaugeContext gaugeContext) {
        this.testRailContext = testRailContext;
        this.gaugeContext = gaugeContext;
    }

    @Override
    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        List<GaugeSpec> result = new ArrayList<>(specData.size());
        for (GaugeSpec gaugeSpec : specData) {
            List<Tagged> scenariosWithTag = gaugeSpec.getScenariosWithTag();
            for (Tagged tagged : scenariosWithTag) {
                if (tagged.hasTag()) {
                    // update
                } else {
                    // new tag
                }
                result.add(gaugeSpec);
            }
        }

//        for (SpecModification specModification : specData) {
//            Collection<String> scenarioModifications = specModification.getHeadings();
//            for (String mod : scenarioModifications) {
//                JSONObject result = send(testRailClient, createJSONObject(specModification, mod));
//                specModification.setTag(mod, "C" + result.get("id"));
//            }
        return result;
    }


    private JSONObject send(APIClient testRailClient, JSONObject obj) throws IOException, APIException {
        return (JSONObject) testRailClient.sendPost("add_case/" + testRailContext.getSectionId(), obj);
    }

    private JSONObject createJSONObject(GaugeSpec specModification, String mod) {
        JSONObject obj = new JSONObject();
//        obj.put("title", mod);
//        obj.put("template_id", testRailContext.getGaugeTemplateId());
//        String specLink = testRailContext.getSpecLink();
//        if (!"".equals(specLink)) {
//            Path specFile = specModification.getSpecFile();
//            Path projectRoot = Paths.get(gaugeContext.getGaugeProjectRoot());
//            String total = specLink + projectRoot.relativize(specFile);
//            obj.put("custom_" + testRailContext.getSpecFieldLabel(), total);
//        }
        return obj;
    }
}
