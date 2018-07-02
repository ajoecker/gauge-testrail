package de.nexible.gauge.testrail.sync;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SpecModifierTest {
    @Test
    @DisplayName("A scenario without tags")
    public void scenarioNoTag() throws IOException {
        String s = "# A spec\n\n" +
                "this is some comment\n" +
                "* a general step\n" +
                "## a scenario\n" +
                "* step 1\n" +
                "* step 2\n";

        String newTag = "C1";
        SpecModifier specModifier = new SpecModifier() {
            @Override
            protected void write(Path path, List<String> lines) throws IOException {
                assertThat(lines).containsSequence("## a scenario", "tags: " + newTag, "* step 1");
            }
        };
        verifyChange(s, newTag, specModifier);
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
        String newTag = "C1";
        SpecModifier specModifier = new SpecModifier() {
            @Override
            protected void write(Path path, List<String> lines) throws IOException {
                assertThat(lines).containsSequence("## a scenario", "tags: smoke, " + newTag, "* step 1");
            }
        };
        verifyChange(s, newTag, specModifier);
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

        String newTag = "C1";
        SpecModifier specModifier = new SpecModifier() {
            @Override
            protected void write(Path path, List<String> lines) throws IOException {
                assertThat(lines).containsSequence("## a scenario", "", "", "tags: smoke, " + newTag, "* step 1");
            }
        };
        verifyChange(s, newTag, specModifier);
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

        String newTag = "C1";
        SpecModifier specModifier = new SpecModifier() {
            @Override
            protected void write(Path path, List<String> lines) throws IOException {
                assertThat(lines).containsSequence("## a scenario", "tags: smoke, " + newTag, "* step 1");
            }
        };
        verifyChange(s, newTag, specModifier);
    }

    private void verifyChange(String s, String newTag, SpecModifier specModifier) throws IOException {
        Path testrailsyn = Files.createTempFile("testrailsyn", ".spec");
        Files.write(testrailsyn, s.getBytes());
        SpecModifications m = new SpecModifications(testrailsyn.toString());
        m.setTag("a scenario", newTag);
        specModifier.persistChanges(ImmutableList.of(m));
        Files.deleteIfExists(testrailsyn);
    }
}
