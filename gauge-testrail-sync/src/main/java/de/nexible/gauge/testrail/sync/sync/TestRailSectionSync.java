package de.nexible.gauge.testrail.sync.sync;

import com.google.common.collect.ImmutableMap;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.sync.context.TestRailSyncContext;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.nexible.gauge.testrail.config.TestRailConfig.parseSectionId;
import static de.nexible.gauge.testrail.config.TestRailConfig.toSectionTag;

public class TestRailSectionSync implements Sync {
    private TestRailSyncContext testRailContext;

    public TestRailSectionSync(TestRailSyncContext testRailContext) {
        this.testRailContext = testRailContext;
    }

    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        List<GaugeSpec> changedData = new ArrayList<>(specData.size());
        for (GaugeSpec s : specData) {
            if (s.hasTag()) {
                checkForSectionUpdate(s);
            } else {
                try {
                    s.setTag(toSectionTag(createNewSection(s)));
                } catch (IOException | APIException e) {
                    // TODO logger
                    return Collections.emptyList();
                }
            }
            changedData.add(s);
        }
        return changedData;
    }

    private int createNewSection(GaugeSpec s) throws IOException, APIException {
        int projectId = testRailContext.projectId();
        APIClient testRailClient = testRailContext.getTestRailClient();
        JSONObject result = (JSONObject) testRailClient.sendPost("add_section/" + projectId, ImmutableMap.of("name", s.getHeading()));
        return (int) result.get("id");
    }

    private void checkForSectionUpdate(GaugeSpec s) {
        int sectionId = parseSectionId(s.getTag());
        try {
            String sectionName = getSectionFromTestRail(sectionId);
            if (s.hasHeadingChanged(sectionName)) {
                updateSectionInTestRail(s.getHeading(), sectionId);
            }
        } catch (IOException | APIException e) {
            // TODO logging
        }
    }

    private void updateSectionInTestRail(String specName, int sectionId) throws IOException, APIException {
        APIClient testRailClient = testRailContext.getTestRailClient();
        testRailClient.sendPost("update_section/" + sectionId, ImmutableMap.of("name", specName));
    }

    private String getSectionFromTestRail(int sectionId) throws IOException, APIException {
        APIClient testRailClient = testRailContext.getTestRailClient();
        JSONObject get = (JSONObject) testRailClient.sendGet("get_section/" + sectionId);
        return String.valueOf(get.get("name"));
    }
}
