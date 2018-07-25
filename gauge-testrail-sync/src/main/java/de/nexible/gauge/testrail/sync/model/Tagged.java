package de.nexible.gauge.testrail.sync.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class Tagged {
    protected String heading;
    protected Optional<String> tag;
    protected List<StepItem> steps;
    protected List<StepItem> comments;
    private boolean hasChanged;

    protected final void setSteps(List<StepItem> steps) {
        this.steps = steps;
    }

    public final List<StepItem> getComments() {
        return Collections.unmodifiableList(comments);
    }

    protected final void setComments(List<StepItem> comments) {
        this.comments = comments;
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
