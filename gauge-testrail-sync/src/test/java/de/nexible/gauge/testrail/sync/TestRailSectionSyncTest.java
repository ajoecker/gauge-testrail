package de.nexible.gauge.testrail.sync;

import com.google.common.collect.ImmutableMap;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.model.Tagged;
import de.nexible.gauge.testrail.sync.sync.TestRailSectionSync;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TestRailSectionSyncTest {
    @Test
    @DisplayName("a new spec is posted to TestRail and section id is saved")
    public void newSpec() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", 13l);
        Mockito.when(client.sendPost("add_section/1", ImmutableMap.of("name","a spec"))).thenReturn(jsonObject);
        TestRailSectionSync testRailSectionSync = new TestRailSectionSync(new TestSyncContext(client));
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(Spec.ProtoSpec.newBuilder().setSpecHeading("a spec").build());
        List<GaugeSpec> gaugeSpecs = testRailSectionSync.sync(Arrays.asList(gaugeSpec));
        Assertions.assertThat(gaugeSpecs).extracting(Tagged::getTag).containsOnly("section_13");
    }

    @Test
    public void hasTagAndNotChanged() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", 13l);
        Mockito.when(client.sendPost("add_section/1", ImmutableMap.of("name","a spec"))).thenReturn(jsonObject);
        TestRailSectionSync testRailSectionSync = new TestRailSectionSync(new TestSyncContext(client));
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(Spec.ProtoSpec.newBuilder().setSpecHeading("a spec").build());
        List<GaugeSpec> gaugeSpecs = testRailSectionSync.sync(Arrays.asList(gaugeSpec));
        Assertions.assertThat(gaugeSpecs).extracting(Tagged::getTag).containsOnly("section_13");
    }

}
