package de.nexible.gauge.testrail.sync;

import com.google.common.collect.ImmutableMap;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.model.Tagged;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static de.nexible.gauge.testrail.sync.GaugeSpecRetrieval.retrieveSpecs;
import static java.nio.file.Files.readAllLines;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestRailSyncTest {
    @Test
    public void specWithNoTagsAtAll_SectionAndCasesAreAdded() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        TestRailSync testRailSync = syncer(client);

        Path dummySpecFile = createDummySpecFile();

        Spec.ProtoScenario.Builder scenario = scenarioBuilder();
        Spec.ProtoSpec.Builder protospec = specBuilder(scenario, dummySpecFile);
        when(client.sendPost(eq("add_section/1"), any())).thenReturn(buildIdBasedResult(25l));
        when(client.sendPost(eq("add_case/25"), any())).thenReturn(buildIdBasedResult(13l));

        GaugeSpec gaugeSpec = doTheSync(testRailSync, protospec);

        assertAll("gaugespec",
                () -> assertThat(gaugeSpec.getTag()).isEqualTo("section_25"),
                () -> assertThat(gaugeSpec.hasBeenTagged()).isTrue(),
                () -> assertThat(gaugeSpec.getScenarios()).extracting(Tagged::getTag).containsOnlyOnce("C13"),
                () -> assertThat(gaugeSpec.getScenarios()).extracting(Tagged::hasBeenTagged).containsOnlyOnce(true));
    }

    @Test
    public void specFileIsUpdatedCorrectlyForNewTags() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        TestRailSync testRailSync = syncer(client);

        Path dummySpecFile = createDummySpecFile();
        Files.write(dummySpecFile, Arrays.asList("# the spec",
                "",
                "## first scenario",
                "* one step",
                "* second step"));

        Spec.ProtoScenario.Builder scenario = scenarioBuilder();
        Spec.ProtoSpec.Builder protospec = specBuilder(scenario, dummySpecFile);
        when(client.sendPost(eq("add_section/1"), any())).thenReturn(buildIdBasedResult(25l));
        when(client.sendPost(eq("add_case/25"), any())).thenReturn(buildIdBasedResult(13l));

        doTheSync(testRailSync, protospec);
        assertThat(readAllLines(dummySpecFile)).containsSequence("# the spec", "tags: section_25", "", "## first scenario", "tags: C13", "* one step", "* second step");
    }

    @Test
    public void specWithTag_SectionIsNotChangedAndCasesAreAdded() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        TestRailSync testRailSync = syncer(client);

        Path dummySpecFile = createDummySpecFile();

        Spec.ProtoScenario.Builder scenario = scenarioBuilder();
        Spec.ProtoSpec.Builder protospec = specBuilder(scenario, dummySpecFile);
        protospec.addTags("section_25");

        when(client.sendPost(eq("add_case/25"), any())).thenReturn(buildIdBasedResult(13l));
        when(client.sendGet("get_section/25")).thenReturn(buildResult("name", protospec.getSpecHeading()));

        GaugeSpec gaugeSpec = doTheSync(testRailSync, protospec);

        verify(client, Mockito.never()).sendPost(eq("add_section/1"), any());

        assertAll("gaugespec",
                () -> assertThat(gaugeSpec.getTag()).isEqualTo("section_25"),
                () -> assertThat(gaugeSpec.hasBeenTagged()).isFalse(),
                () -> assertThat(gaugeSpec.getScenarios()).extracting(Tagged::getTag).containsOnlyOnce("C13"));
    }

    @Test
    public void specWithNonTestRail_SectionAndCasesAreAdded() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        TestRailSync testRailSync = syncer(client);

        Path dummySpecFile = createDummySpecFile();

        Spec.ProtoScenario.Builder scenario = scenarioBuilder();
        Spec.ProtoSpec.Builder protospec = specBuilder(scenario, dummySpecFile);
        protospec.addTags("another tag");

        when(client.sendPost(eq("add_case/25"), any())).thenReturn(buildIdBasedResult(13l));
        when(client.sendPost(eq("add_section/1"), any())).thenReturn(buildIdBasedResult(25l));

        GaugeSpec gaugeSpec = doTheSync(testRailSync, protospec);

        assertAll("gaugespec",
                () -> assertThat(gaugeSpec.getTag()).isEqualTo("section_25"),
                () -> assertThat(gaugeSpec.hasBeenTagged()).isTrue(),
                () -> assertThat(gaugeSpec.getScenarios()).extracting(Tagged::getTag).containsOnlyOnce("C13"));
    }

    @Test
    public void specWithChangedHeading_SectionIsUpdated() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        TestRailSync testRailSync = syncer(client);

        Path dummySpecFile = createDummySpecFile();

        Spec.ProtoScenario.Builder scenario = scenarioBuilder();
        scenario.addTags("C13");
        Spec.ProtoSpec.Builder protospec = specBuilder(scenario, dummySpecFile);
        protospec.addTags("section_25");

        when(client.sendGet("get_section/25")).thenReturn(buildResult("name", "a different spec heading"));
        when(client.sendPost(eq("update_case/13"), any())).thenReturn(buildResult("id", "13"));

        GaugeSpec gaugeSpec = doTheSync(testRailSync, protospec);
        verify(client).sendPost("update_section/25", ImmutableMap.of("name", "the spec"));

        assertAll("gaugespec",
                () -> assertThat(gaugeSpec.getTag()).isEqualTo("section_25"),
                () -> assertThat(gaugeSpec.hasBeenTagged()).isFalse(),
                () -> assertThat(gaugeSpec.getHeading()).isEqualTo("the spec"),
                () -> assertThat(gaugeSpec.getScenarios()).extracting(Tagged::hasBeenTagged).containsOnly(false));
    }

    @Test
    public void specFileIsUpdatedCorrectlyForExistingTags() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        TestRailSync testRailSync = syncer(client);

        Path dummySpecFile = createDummySpecFile();
        Files.write(dummySpecFile, Arrays.asList("# the spec",
                "tags: section_25",
                "## first scenario",
                "tags: smoke, C13",
                "* one step",
                "* second step"));

        Spec.ProtoScenario.Builder scenario = scenarioBuilder();
        scenario.addTags("C13");
        scenario.addTags("smoke");
        Spec.ProtoSpec.Builder protospec = specBuilder(scenario, dummySpecFile);
        protospec.addTags("section_25");

        when(client.sendGet("get_section/25")).thenReturn(buildResult("name", "a different spec heading"));
        when(client.sendPost(eq("update_case/13"), any())).thenReturn(buildResult("id", "13"));

        doTheSync(testRailSync, protospec);
        verify(client).sendPost("update_section/25", ImmutableMap.of("name", "the spec"));

        doTheSync(testRailSync, protospec);
        assertThat(readAllLines(dummySpecFile)).containsSequence("# the spec", "tags: section_25", "## first scenario", "tags: smoke, C13", "* one step", "* second step");
    }

    @Test
    public void updateTestCases() throws IOException, APIException {
        APIClient client = Mockito.mock(APIClient.class);
        TestRailSync testRailSync = syncer(client);

        Path dummySpecFile = createDummySpecFile();

        Spec.ProtoScenario.Builder scenario = scenarioBuilder();
        scenario.addTags("C13");
        Spec.ProtoSpec.Builder protospec = specBuilder(scenario, dummySpecFile);
        protospec.addTags("section_25");

        when(client.sendGet("get_section/25")).thenReturn(buildResult("name", "a spec"));
        when(client.sendPost(eq("update_case/13"), any())).thenReturn(buildResult("id", "13"));

        GaugeSpec gaugeSpec = doTheSync(testRailSync, protospec);

        verify(client).sendPost(eq("update_case/13"), any());

        assertAll("gaugespec",
                () -> assertThat(gaugeSpec.getTag()).isEqualTo("section_25"),
                () -> assertThat(gaugeSpec.hasBeenTagged()).isFalse(),
                () -> assertThat(gaugeSpec.getHeading()).isEqualTo("the spec"),
                () -> assertThat(gaugeSpec.getScenarios()).extracting(Tagged::hasBeenTagged).containsOnly(false));
    }

    private TestRailSync syncer(APIClient client) {
        return new TestRailSync(new TestSyncContext(client), "");
    }

    private GaugeSpec doTheSync(TestRailSync testRailSync, Spec.ProtoSpec.Builder protospec) {
        List<GaugeSpec> gaugeSpecs = retrieveSpecs(asList(protospec.build()));
        List<GaugeSpec> sync = testRailSync.sync(gaugeSpecs);

        return sync.get(0);
    }

    private JSONObject buildIdBasedResult(long l) {
        JSONObject sectionJSON = new JSONObject();
        sectionJSON.put("id", l);
        return sectionJSON;
    }

    private JSONObject buildResult(String key, String value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(key, value);
        return jsonObject;
    }

    private Path createDummySpecFile() throws IOException {
        Path specfile = Files.createTempFile("specfile", ".spec");
        specfile.toFile().deleteOnExit();
        return specfile;
    }

    private Spec.ProtoSpec.Builder specBuilder(Spec.ProtoScenario.Builder scenario, Path dummySpecFile) throws IOException {
        Spec.ProtoItem protoItem = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Scenario).setScenario(scenario).build();
        return Spec.ProtoSpec.newBuilder().setFileName(dummySpecFile.toString()).setSpecHeading("the spec").addItems(protoItem);
    }

    private Spec.ProtoScenario.Builder scenarioBuilder() {
        return Spec.ProtoScenario.newBuilder()
                .setScenarioHeading("first scenario")
                .addScenarioItems(createStep("one step"))
                .addScenarioItems(createStep("second step"));
    }

    private Spec.ProtoItem createStep(String step) {
        Spec.ProtoStep protostep = Spec.ProtoStep.newBuilder().setParsedText(step).build();
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(protostep).build();
    }
}
