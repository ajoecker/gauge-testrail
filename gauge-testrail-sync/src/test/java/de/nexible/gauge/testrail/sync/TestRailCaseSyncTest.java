package de.nexible.gauge.testrail.sync;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.model.Tagged;
import de.nexible.gauge.testrail.sync.sync.TestRailCaseSync;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class TestRailCaseSyncTest {
    @Test
    public void foo() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        JSONObject res = new JSONObject();
        res.put("id", "234");
        Mockito.when(client.sendPost(eq("add_case/12"), any())).thenReturn(res);

        TestRailCaseSync testRailCaseSync = new TestRailCaseSync(new TestSyncContext(client));

        List<GaugeSpec> specData = new ArrayList<>();

        Spec.ProtoSpec spec = Spec.ProtoSpec.newBuilder().addTags("section_12").setFileName("file").setSpecHeading("spec").build();
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec);

        Spec.ProtoStep step = Spec.ProtoStep.newBuilder().setParsedText("step").build();
        Spec.ProtoItem stepItem = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(step).build();
        Spec.ProtoScenario scenario = Spec.ProtoScenario.newBuilder().setScenarioHeading("scenario").addScenarioItems(stepItem).build();

        Tagged tagged = gaugeSpec.addScenario(scenario);
        tagged.setTag("C123");
        specData.add(gaugeSpec);

        List<GaugeSpec> sync = testRailCaseSync.sync(specData);

        verify(client, atLeastOnce()).sendPost(eq("add_case/12"), any());

        assertThat(sync).flatExtracting(GaugeSpec::getScenarios).extracting(GaugeScenario::hasBeenTagged).containsOnly(true);
        assertThat(sync).flatExtracting(GaugeSpec::getScenarios).extracting(GaugeScenario::getTag).containsOnly("C234");
    }
}
