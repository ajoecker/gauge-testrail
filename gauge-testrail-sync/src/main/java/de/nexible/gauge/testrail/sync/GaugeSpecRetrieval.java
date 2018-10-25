package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import de.nexible.gauge.testrail.sync.model.StepItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static de.nexible.gauge.testrail.sync.model.StepItem.newStepItem;

public class GaugeSpecRetrieval {
    public static List<GaugeSpec> retrieveSpecs(List<Spec.ProtoSpec> protoSpecList) {
        return protoSpecList.stream().map(GaugeSpecRetrieval::getSpecs).collect(Collectors.toList());
    }

    private static GaugeSpec getSpecs(Spec.ProtoSpec spec) {
        return spec.getItemsList().stream()
                .filter(item -> item.getItemType() == Spec.ProtoItem.ItemType.Scenario)
                .map(item -> item.getScenario())
                .collect(() -> GaugeSpec.newInstance(spec), GaugeSpec::addScenario, (x, y) -> {
                });
    }

    public static List<StepItem> extractComments(List<Spec.ProtoItem> itemsList) {
        return itemsList.stream()
                .filter(GaugeSpecRetrieval::isComment)
                .map(Spec.ProtoItem::getComment)
                .map(Spec.ProtoComment::getText).filter(s -> !isNullOrEmpty(s.trim())).map(StepItem::newStepItem)
                .collect(Collectors.toList());
    }

    public static List<StepItem> extractSteps(List<Spec.ProtoItem> protoItems) {
        ArrayList<StepItem> steps = new ArrayList<>();
        stepExtraction(protoItems, 0, steps);
        return steps;
    }

    private static boolean isComment(Spec.ProtoItem protoItem) {
        return protoItem.getItemType() == Spec.ProtoItem.ItemType.Comment;
    }

    private static void stepExtraction(List<Spec.ProtoItem> protoItems, int level, List<StepItem> steps) {
        for (Spec.ProtoItem item : protoItems) {
            if (item.getItemType() == Spec.ProtoItem.ItemType.Step) {
                addStep(steps, item.getStep(), level);
            } else if (item.getItemType() == Spec.ProtoItem.ItemType.Concept) {
                Spec.ProtoConcept concept = item.getConcept();
                addStep(steps, concept.getConceptStep(), level);
                stepExtraction(concept.getStepsList(), level + 1, steps);
            }
        }
    }

    private static void addStep(List<StepItem> steps, Spec.ProtoStep step, int level) {
        steps.add(newStepItem(step.getActualText(), level));
        extractFragements(step, level, steps);
    }

    private static void extractFragements(Spec.ProtoStep step, int level, List<StepItem> steps) {
        step.getFragmentsList().stream()
                .filter(fragment -> fragment.getFragmentType() == Spec.Fragment.FragmentType.Parameter)
                .forEach(fragment -> handleFragment(fragment, level, steps));
    }

    private static void handleFragment(Spec.Fragment fragment, int level, List<StepItem> steps) {
        Spec.Parameter parameter = fragment.getParameter();
        if (parameter.getParameterType() == Spec.Parameter.ParameterType.Table) {
            handleTable(parameter, level, steps);
        }
    }

    private static void handleTable(Spec.Parameter parameter, int level, List<StepItem> steps) {
        Spec.ProtoTable table = parameter.getTable();
        steps.add(newStepItem(tableJoin(table.getHeaders(), "|:", "|||:"), level));
        table.getRowsList().stream().map(row -> newStepItem(tableJoin(row, "|", "||"), level)).forEach(steps::add);
    }

    private static String tableJoin(Spec.ProtoTableRow tableRow, String delimiter, String prefix) {
        return tableRow.getCellsList().stream().collect(Collectors.joining(delimiter, prefix, ""));
    }
}
