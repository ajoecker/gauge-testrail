package de.nexible.gauge.testrail.sync.model;

import com.thoughtworks.gauge.Spec;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Tagged {
    protected String heading;
    protected Optional<String> tag;
    protected List<String> steps;
    private boolean hasChanged;

    protected final void setSteps(List<Spec.ProtoItem> protoItems) {
        steps = protoItems.stream()
                .filter(protoItem -> protoItem.getItemType() == Spec.ProtoItem.ItemType.Step)
                .map(Spec.ProtoItem::getStep)
                .map(Spec.ProtoStep::getParsedText)
                .collect(Collectors.toList());
    }


    public List<String> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public boolean hasTag() {
        return tag.isPresent();
    }

    public String getTag() {
        return tag.get();
    }

    public boolean hasHeadingChanged(String name) {
        return !name.equals(heading);
    }

    public String getHeading() {
        return heading;
    }

    public void setTag(String s) {
        this.tag = Optional.of(s);
        this.hasChanged = true;
    }

    public boolean hasBeenTagged() {
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
