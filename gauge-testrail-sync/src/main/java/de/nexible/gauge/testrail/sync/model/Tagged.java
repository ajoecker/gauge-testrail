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

public class Tagged {
    protected String heading;
    protected Optional<String> tag;
    protected List<String> steps;
    protected List<String> comments;
    private boolean hasChanged;

    protected final void setSteps(List<Spec.ProtoItem> protoItems) {
        steps = new ArrayList<>();
        for (Spec.ProtoItem protoItem : protoItems) {
            if (isConceptOrStep(protoItem)) {
                Spec.ProtoStep protoStep = extractStep(protoItem);
                steps.add(protoStep.getActualText());
                for (Spec.Fragment fr : protoStep.getFragmentsList()) {
                    if (fr.getFragmentType() == Spec.Fragment.FragmentType.Parameter) {
                        Spec.Parameter parameter = fr.getParameter();
                        if (parameter.getParameterType() == Spec.Parameter.ParameterType.Table) {
                            Spec.ProtoTable table = parameter.getTable();
                            Spec.ProtoTableRow headers = table.getHeaders();
                            String header = headers.getCellsList().stream().collect(Collectors.joining("|:", "|||:", ""));
                            steps.add(header);
                            for (Spec.ProtoTableRow row : table.getRowsList()) {
                                String rowContent = row.getCellsList().stream().collect(Collectors.joining("|", "||", ""));
                                steps.add(rowContent);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isConceptOrStep(Spec.ProtoItem protoItem) {
        return protoItem.getItemType() == Spec.ProtoItem.ItemType.Concept || protoItem.getItemType() == Spec.ProtoItem.ItemType.Step;
    }

    private boolean isComment(Spec.ProtoItem protoItem) {
        return protoItem.getItemType() == Spec.ProtoItem.ItemType.Comment;
    }

    private Spec.ProtoStep extractStep(Spec.ProtoItem protoItem) {
        return protoItem.getItemType() == Spec.ProtoItem.ItemType.Step ? protoItem.getStep() : protoItem.getConcept().getConceptStep();
    }

    public final List<String> getComments() {
        return Collections.unmodifiableList(comments);
    }

    protected final void setComments(List<Spec.ProtoItem> itemsList) {
        comments = itemsList.stream()
                .filter(this::isComment)
                .map(Spec.ProtoItem::getComment)
                .map(Spec.ProtoComment::getText).filter(s -> !isNullOrEmpty(s.trim()))
                .collect(Collectors.toList());
    }

    public final List<String> getSteps() {
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
