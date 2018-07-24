package de.nexible.gauge.testrail.sync.model;

public class StepItem {
    private String step;
    private int level;

    private StepItem() {
        // static construct
    }

    public static StepItem newStepItem(String step, int level) {
        StepItem stepItem = new StepItem();
        stepItem.step = step;
        stepItem.level = level;
        return stepItem;
    }

    public static StepItem newStepItem(String step) {
        return newStepItem(step, 0);
    }

    @Override
    public String toString() {
        return "StepItem{" +
                "step='" + step + '\'' +
                ", level=" + level +
                '}';
    }

    public String step() {
        return step;
    }
}
