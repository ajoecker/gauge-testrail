package de.nexible.gauge.testrail.sync;

import com.google.common.collect.ImmutableList;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.config.GaugeContext;
import de.nexible.gauge.testrail.config.GaugeDefaultContext;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TestRailConnector {
    private TestRailSynContext testRailContext;
    private GaugeContext gaugeContext;

    public TestRailConnector(TestRailSynContext testRailContext, GaugeContext gaugeContext) {
        this.testRailContext = testRailContext;
        this.gaugeContext = gaugeContext;
    }

    public void upload(List<SpecModification> modifications) throws IOException, APIException {
        APIClient testRailClient = testRailContext.getTestRailClient();
        for (SpecModification specModification : modifications) {
            JSONObject obj = new JSONObject();
            obj.put("title", specModification.getScenarioHeading());
            obj.put("template_id", testRailContext.getGaugeTemplateId());


            String specLink = testRailContext.getSpecLink();
            if (!"".equals(specLink)) {
                Path specFile = specModification.getSpecFile();
                Path projectRoot = Paths.get(gaugeContext.getGaugeProjectRoot());
                String total = specLink + projectRoot.relativize(specFile);
                obj.put("custom_" + testRailContext.getSpecFieldLabel(), total);
            }
            JSONObject post = (JSONObject) testRailClient.sendPost("add_case/" + testRailContext.getSectionId(), obj);
            specModification.setTag("C" + post.get("id"));
        }
    }
}
