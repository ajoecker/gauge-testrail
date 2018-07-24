package de.nexible.gauge.testrail.sync.model;

import com.thoughtworks.gauge.Spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.*;
import static de.nexible.gauge.testrail.sync.model.StepItem.newStepItem;

public class Tagged {
    protected String heading;
    protected Optional<String> tag;
    protected List<StepItem> steps;
    protected List<StepItem> comments;
    private boolean hasChanged;


    protected final void setSteps(List<Spec.ProtoItem> protoItems) {
        steps = new ArrayList<>();
        stepExtraction(protoItems, 0);
    }

    private void stepExtraction(List<Spec.ProtoItem> protoItems, int level) {
        for (Spec.ProtoItem item : protoItems) {
            if (item.getItemType() == Spec.ProtoItem.ItemType.Step) {
                Spec.ProtoStep step = item.getStep();
                steps.add(newStepItem(step.getActualText(), level));
                handleFragments(step, level);
            } else if (item.getItemType() == Spec.ProtoItem.ItemType.Concept) {
                Spec.ProtoConcept concept = item.getConcept();
                Spec.ProtoStep step = concept.getConceptStep();
                steps.add(newStepItem(step.getActualText(), level));
                handleFragments(step, level);
                stepExtraction(concept.getStepsList(), level + 1);
            }
        }
    }

    private void handleFragments(Spec.ProtoStep step, int level) {
        step.getFragmentsList().stream()
                .filter(fragment -> fragment.getFragmentType() == Spec.Fragment.FragmentType.Parameter)
                .forEach(fragment -> handleFragment(fragment, level));
    }

    private void handleFragment(Spec.Fragment fragment, int level) {
        Spec.Parameter parameter = fragment.getParameter();
        if (parameter.getParameterType() == Spec.Parameter.ParameterType.Table) {
            handleTable(parameter, level);
        }
    }

    private void handleTable(Spec.Parameter parameter, int level) {
        Spec.ProtoTable table = parameter.getTable();
        Spec.ProtoTableRow headers = table.getHeaders();
        String header = headers.getCellsList().stream().collect(Collectors.joining("|:", "|||:", ""));
        steps.add(newStepItem(header, level));
        for (Spec.ProtoTableRow row : table.getRowsList()) {
            String rowContent = row.getCellsList().stream().collect(Collectors.joining("|", "||", ""));
            steps.add(newStepItem(rowContent, level));
        }
    }

    private boolean isComment(Spec.ProtoItem protoItem) {
        return protoItem.getItemType() == Spec.ProtoItem.ItemType.Comment;
    }

    public final List<StepItem> getComments() {
        return Collections.unmodifiableList(comments);
    }

    protected final void setComments(List<Spec.ProtoItem> itemsList) {
        comments = itemsList.stream()
                .filter(this::isComment)
                .map(Spec.ProtoItem::getComment)
                .map(Spec.ProtoComment::getText).filter(s -> !isNullOrEmpty(s.trim())).map(StepItem::newStepItem)
                .collect(Collectors.toList());
    }

    public final List<StepItem> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public final boolean hasTag() {
        return tag.isPresent();
    }

    public final String getTag() {
        return tag.get();
    }

    public final boolean hasHeadingChanged(String name) {
        return !name.equals(heading);
    }

    public final String getHeading() {
        return heading;
    }

    public final void setTag(String s) {
        this.tag = Optional.of(s);
        this.hasChanged = true;
    }

    public final boolean hasBeenTagged() {
        return hasTag() && hasChanged;
    }

    @Override
    public String toString() {
        return "Tagged{" +
                "heading='" + heading + '\'' +
                ", tag=" + tag +
                ", steps=" + steps +
                ", hasChanged=" + hasChanged +
                '}';
    }

    protected static Optional<String> findTestRailTag(List<String> tags, Predicate<String> filter) {
        return tags.stream().filter(filter).findAny();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tagged)) return false;
        Tagged tagged = (Tagged) o;
        return hasChanged == tagged.hasChanged &&
                Objects.equals(heading, tagged.heading) &&
                Objects.equals(tag, tagged.tag) &&
                Objects.equals(steps, tagged.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heading, tag, steps);
    }
}
