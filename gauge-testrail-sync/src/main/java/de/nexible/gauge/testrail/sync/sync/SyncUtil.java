package de.nexible.gauge.testrail.sync.sync;

import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class SyncUtil {
    private SyncUtil() {
        // static
    }

    public static String getCaseText(GaugeSpec spec, GaugeScenario scenario) {
        StringBuilder comments = new StringBuilder();
        comments.append(formatComments(spec.getComments()));
        comments.append(formatComments(scenario.getComments()));
        if (comments.length() > 0) {
            comments.append("\n");
        }
        StringBuilder steps = new StringBuilder();
        steps.append(formatSteps(spec.getSteps()));
        if (steps.length() > 0) {
            steps.append("\n");
        }
        steps.append(formatSteps(scenario.getSteps()));

        return comments.append(steps).toString();
    }

    private static String formatComments(List<String> comments) {
        return comments.stream().collect(joining("\n"));
    }

    private static String formatSteps(List<String> steps) {
        return steps.stream().map(s -> "* " + s).collect(joining("\n"));
    }
}
