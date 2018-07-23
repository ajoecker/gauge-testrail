package de.nexible.gauge.testrail.sync;

import com.google.common.collect.ImmutableList;
import com.gurock.testrail.APIClient;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.config.TestRailContext;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.model.Tagged;
import de.nexible.gauge.testrail.sync.sync.SpecModifier;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecModifierTest {
    private TestRailContext context = new TestSyncContext(Mockito.mock(APIClient.class));

    @Test
    @DisplayName("A scenario without tags")
    public void scenarioNoTag() throws IOException {
        String s = "# A spec\n\n" +
                "this is some comment\n" +
                "* a general step\n" +
                "## a scenario\n" +
                "* step 1\n" +
                "* step 2\n";

        Spec.ProtoSpec.Builder spec = spec(scenario(step("step 1"), step("step 2")));
        SpecModifier specModifier = new TestSpecModifier(context, "## a scenario", "tags: C1", "* step 1");
        verifyChange(spec, specModifier, s, defaultTagScenario());
    }

    @Test
    @DisplayName("A scenario with already defined tags")
    public void scenarioWithTags() throws IOException {
        String s = "# A spec\n\n" +
                "this is some comment\n" +
                "* a general step\n" +
                "## a scenario\n" +
                "tags: smoke\n" +
                "* step 1\n" +
                "* step 2\n";
        Spec.ProtoSpec.Builder spec = spec(scenario(step("step 1"), step("step 2"), "smoke"));
        SpecModifier specModifier = new TestSpecModifier(context, "## a scenario", "tags: smoke, C1", "* step 1");
        verifyChange(spec, specModifier, s, defaultTagScenario());
    }

    @Test
    @DisplayName("A scenario with tags and empty lines")
    public void scenarioWithTagsAndNewLines() throws IOException {
        String s = "# A spec\n\n" +
                "this is some comment\n" +
                "* a general step\n" +
                "## a scenario\n" +
                "\n\n" +
                "tags: smoke\n" +
                "* step 1\n" +
                "* step 2\n";

        Spec.ProtoSpec.Builder spec = spec(scenario(step("step 1"), step("step 2"), "smoke"));
        SpecModifier specModifier = new TestSpecModifier(context, "## a scenario", "", "", "tags: smoke, C1", "* step 1");
        verifyChange(spec, specModifier, s, defaultTagScenario());
    }

    @Test
    @DisplayName("Two scenarios with one tagged, one not tagged")
    public void multipleScenariosOneWithTag() throws IOException {
        String s = "# A spec\n\n" +
                "this is some comment\n" +
                "* a general step\n" +
                "## a scenario with\n" +
                "tags: smoke\n" +
                "* step 1\n" +
                "* step 2\n" +
                "## a scenario\n" +
                "tags: smoke\n" +
                "* step 1\n" +
                "* step 2\n";
        Spec.ProtoItem scenaro1 = scenario(step("step 1"), step("step 2"), "smoke");
        Spec.ProtoItem scenaro2 = scenario(step("step 1"), step("step 2"), "smoke");
        Spec.ProtoSpec.Builder spec = spec(scenaro1, scenaro2);

        SpecModifier specModifier = new TestSpecModifier(context, "## a scenario with", "tags: smoke", "* step 1", "* step 2", "## a scenario", "tags: smoke, C1", "* step 1");
        verifyChange(spec, specModifier, s, defaultTagScenario());
    }

    @Test
    @DisplayName("Two scenarios with two tagged")
    public void multipleScenariosAllWithTag() throws IOException {
        String s = "# A spec\n\n" +
                "this is some comment\n" +
                "* a general step\n" +
                "## a scenario with\n" +
                "tags: smoke\n" +
                "* step 1\n" +
                "* step 2\n" +
                "## a scenario\n" +
                "tags: smoke\n" +
                "* step 1\n" +
                "* step 2\n";
        Spec.ProtoItem scenaro1 = scenario(step("step 1"), step("step 2"), "smoke");
        Spec.ProtoItem scenaro2 = scenario(step("step 1"), step("step 2"), "smoke");
        Spec.ProtoSpec.Builder spec = spec(scenaro1, scenaro2);

        SpecModifier specModifier = new TestSpecModifier(context, "## a scenario with", "tags: smoke, C3", "* step 1", "* step 2", "## a scenario", "tags: smoke, C1", "* step 1");
        verifyChange(spec, specModifier, s, defaultTagScenario(), tagScenario("a scenario with", "C3"));
    }

    private void verifyChange(Spec.ProtoSpec.Builder spec, SpecModifier specModifier, String s, Spec.ProtoScenario... scenarios) throws IOException {
        Path testrailsyn = Files.createTempFile("testrailsyn", ".spec");
        Files.write(testrailsyn, s.getBytes());
        spec.setFileName(testrailsyn.toString());
        GaugeSpec gaugeSpec = GaugeSpec.newInstance(spec.build());
        for (Spec.ProtoScenario scenario : scenarios) {
            Tagged tagged = gaugeSpec.addScenario(scenario);
            tagged.setTag(scenario.getTags(0));
        }
        specModifier.sync(ImmutableList.of(gaugeSpec));
        Files.deleteIfExists(testrailsyn);
    }

    private Spec.ProtoScenario defaultTagScenario() {
        return tagScenario("a scenario", "C1");
    }

    private Spec.ProtoScenario tagScenario(String heading, String tag) {
        return Spec.ProtoScenario.newBuilder().setScenarioHeading(heading).addTags(tag).build();
    }

    private Spec.ProtoSpec.Builder spec(Spec.ProtoItem... scenarios) {
        Spec.ProtoSpec.Builder spec = Spec.ProtoSpec.newBuilder().setSpecHeading("A spec").addItems(comment()).addItems(generalStep());
        for (Spec.ProtoItem s : scenarios) {
            spec.addItems(s);
        }
        return spec;
    }

    private Spec.ProtoItem scenario(Spec.ProtoItem step1, Spec.ProtoItem step2, String... tags) {
        Spec.ProtoScenario.Builder builder = Spec.ProtoScenario.newBuilder().addScenarioItems(step1).addScenarioItems(step2);
        for (String s : tags) {
            builder.addTags(s);
        }
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Scenario).setScenario(builder.build()).build();
    }

    private Spec.ProtoItem step(String s2) {
        Spec.ProtoStep theStep1 = Spec.ProtoStep.newBuilder().setActualText(s2).build();
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(theStep1).build();
    }

    private Spec.ProtoItem generalStep() {
        Spec.ProtoStep theStep = Spec.ProtoStep.newBuilder().setActualText("a general step").build();
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(theStep).build();
    }

    private Spec.ProtoItem comment() {
        Spec.ProtoComment theComment = Spec.ProtoComment.newBuilder().setText("this is some comment").build();
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Comment).setComment(theComment).build();
    }

    private static final class TestSpecModifier extends SpecModifier {
        private boolean called;
        private String[] args;

        TestSpecModifier(TestRailContext testRailContext, String... args) {
            super(testRailContext);
            this.args = args;
        }

        @Override
        public List<GaugeSpec> sync(List<GaugeSpec> mods) {
            called = false;
            List<GaugeSpec> sync = super.sync(mods);
            if (!called) {
                throw new AssertionError("write result was not called !");
            }
            return sync;
        }

        @Override
        protected void write(Path path, List<String> lines) throws IOException {
            called = true;
            assertThat(lines).containsSequence(args);
        }
    }
}
