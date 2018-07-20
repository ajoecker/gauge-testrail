package de.nexible.gauge.testrail.sync;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.model.Tagged;
import de.nexible.gauge.testrail.sync.sync.TestRailCaseSync;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class TestRailCaseSyncTest {
    @Test
    public void newlyTaggedScenarioIsUploadedAsNewCase() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        JSONObject res = new JSONObject();
        res.put("id", "234");
        Mockito.when(client.sendPost(eq("add_case/12"), any())).thenReturn(res);

        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().addTags("section_12").setFileName("file").setSpecHeading("spec").build();
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec);

        Spec.ProtoScenario scenario = Spec.ProtoScenario.newBuilder().setScenarioHeading("scenario")
                .addScenarioItems(createStep("step"))
                .build();

        gaugeSpec.addScenario(scenario);

        TestRailCaseSync testRailCaseSync = new TestRailCaseSync(new TestSyncContext(client));
        List<GaugeSpec> sync = testRailCaseSync.sync(Arrays.asList(gaugeSpec));

        verify(client, atLeastOnce()).sendPost(eq("add_case/12"), any());

        assertThat(sync).flatExtracting(GaugeSpec::getScenarios).extracting(GaugeScenario::hasBeenTagged).containsOnly(true);
        assertThat(sync).flatExtracting(GaugeSpec::getScenarios).extracting(GaugeScenario::getTag).containsOnly("C234");
    }

    @Test
    public void alreadyTaggedScenarioIsUpdated() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        JSONObject res = new JSONObject();
        res.put("id", "234");
        Mockito.when(client.sendPost(eq("update_case/234"), any())).thenReturn(res);

        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().addTags("section_12").setFileName("file").setSpecHeading("spec").build();
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec);

        Spec.ProtoScenario scenario = Spec.ProtoScenario.newBuilder().setScenarioHeading("scenario").addTags("C234")
                .addScenarioItems(createStep("step"))
                .build();

        gaugeSpec.addScenario(scenario);

        TestRailCaseSync testRailCaseSync = new TestRailCaseSync(new TestSyncContext(client));
        List<GaugeSpec> sync = testRailCaseSync.sync(Arrays.asList(gaugeSpec));

        verify(client, atLeastOnce()).sendPost(eq("update_case/234"), any());

        assertThat(sync).flatExtracting(GaugeSpec::getScenarios).extracting(GaugeScenario::hasBeenTagged).containsOnly(false);
        assertThat(sync).flatExtracting(GaugeSpec::getScenarios).extracting(GaugeScenario::getTag).containsOnly("C234");
    }

    private Spec.ProtoItem createStep(String stepDesc) {
        Spec.ProtoStep step = Spec.ProtoStep.newBuilder().setParsedText(stepDesc).build();
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(step).build();
    }
}
