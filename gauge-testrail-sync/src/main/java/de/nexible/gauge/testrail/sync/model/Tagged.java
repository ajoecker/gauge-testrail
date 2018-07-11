package de.nexible.gauge.testrail.sync.model;

import java.util.Objects;
import java.util.Optional;

public class Tagged {
    protected String heading;
    protected Optional<String> tag;
    private boolean hasChanged;

    public static Tagged newInstance(String heading, Optional<String> tag) {
        Tagged tagged = new Tagged();
        tagged.heading = heading;
        tagged.tag = tag;
        tagged.hasChanged = false;
        return tagged;
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
                Objects.equals(tag, tagged.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heading, tag, hasChanged);
    }
}
