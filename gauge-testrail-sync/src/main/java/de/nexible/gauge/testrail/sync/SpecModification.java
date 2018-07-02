package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SpecModification {
    private Path specFile;
    private String scenarioHeading;
    private String tag;

    public SpecModification(String specFileName) {
        this.specFile = Paths.get(specFileName);
    }

    public Path getSpecFile() {
        return specFile;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public boolean isRelevantScenario(String line) {
        return line.startsWith("##") && specFile.equals(modifyLine(line));
    }

    private String modifyLine(String line) {
        return line.substring(3).trim();
    }

    public void add(Spec.ProtoScenario scenario) {
        scenarioHeading = scenario.getScenarioHeading();
    }

    public String getScenarioHeading() {
        return scenarioHeading;
    }

    @Override
    public String toString() {
        return "SpecModification{" +
                "scenarioHeading=" + scenarioHeading +
                ", specFile=" + specFile +
                '}';
    }
}
