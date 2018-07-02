package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.gauge.GaugeConnector;
import de.nexible.gauge.testrail.sync.gauge.GaugeStepRetriever;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class TestRailSync {
    private static final Pattern TESTRAIL_PATTERN = Pattern.compile("^[Cc]\\d+$");

    public static void main(String[] args) throws IOException {
        new TestRailSync().start();
    }

    private void start() throws IOException {
        GaugeConnector gaugeConnector = new GaugeConnector(new GaugeStepRetriever());
        sync(gaugeConnector.connect());
    }

    private void sync(List<Spec.ProtoSpec> protoSpecList) throws IOException {
        // how to persist a changed Spec.ProtoSpec ?
        for (Spec.ProtoSpec spec : protoSpecList) {
            String specFileName = spec.getFileName();
            System.out.println("ORIGINAL NAME: " + specFileName);
            String before = specFileName.substring(0, specFileName.lastIndexOf('.'));
            Path path = Paths.get(before + "_01" + specFileName.substring(specFileName.lastIndexOf('.')));
            try (OutputStream os = Files.newOutputStream(path)) {
                os.write(spec.toString().getBytes());
            }
        }
    }

    private void syncScenario(String specFileName, Spec.ProtoScenario scenario) {
        Optional<String> anyTestRailTag = scenario.getTagsList().stream().filter(this::isTestRailTag).findAny();
        if (anyTestRailTag.isPresent()) {
            // already a tag
            // update ?
            System.out.println(scenario.getScenarioHeading() + " has already a TestRail tag " + anyTestRailTag.get());
        } else {
            // no tag
            // upload to TestRail
            // tag scenario
            System.out.println(scenario.getScenarioHeading() + " has NO testrail tag");
            System.out.println("So send it to " + System.getenv("testrail.url"));
        }
    }

    private boolean isTestRailTag(String s) {
        return TESTRAIL_PATTERN.matcher(s).find();
    }
}
