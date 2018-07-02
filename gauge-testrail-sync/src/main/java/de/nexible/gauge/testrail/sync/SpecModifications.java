package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A data class to represent a modification of a scenario
 *
 * @author ajoecker
 */
public class SpecModifications {
    private Path specFile;
    private Map<String, String> scenarioHeadingToTag;

    public SpecModifications(String specFileName) {
        this.specFile = Paths.get(specFileName);
        this.scenarioHeadingToTag = new HashMap<>();
    }

    /**
     * Returns the specification file in which the scenario can be found
     *
     * @return
     */
    public Path getSpecFile() {
        return specFile;
    }

    /**
     * Sets the new tag from TestRail
     *
     * @param tag
     */
    public void setTag(String heading, String tag) {
        scenarioHeadingToTag.put(heading, tag);
    }

    public String getTag(String heading) {
        return scenarioHeadingToTag.get(heading);
    }

    /**
     * Returns whether the given heading is the same as the one for this modification.
     *
     * @param line
     * @return
     */
    public Optional<String> isRelevantScenario(String line) {
        if (line.startsWith("##")) {
            String modified = modifyLine(line);
            return scenarioHeadingToTag.containsKey(modified) ? Optional.of(modified): Optional.empty();
        }
        return Optional.empty();
    }

    private String modifyLine(String line) {
        // chops of the ## at a scenario beginning
        return line.substring(3).trim();
    }

    public void add(Spec.ProtoScenario scenario) {
        scenarioHeadingToTag.put(scenario.getScenarioHeading(), "");
    }

    @Override
    public String toString() {
        return "SpecModifications{" +
                "scenarioHeadingToTag=" + scenarioHeadingToTag +
                ", specFile=" + specFile +
                '}';
    }

    public boolean hasModifications() {
        return !scenarioHeadingToTag.isEmpty();
    }

    public Collection<String> getHeadings() {
        return scenarioHeadingToTag.keySet();
    }
}
