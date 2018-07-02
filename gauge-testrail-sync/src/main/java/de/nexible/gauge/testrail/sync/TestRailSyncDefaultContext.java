package de.nexible.gauge.testrail.sync;

import de.nexible.gauge.testrail.config.TestRailDefaultContext;

/**
 * A {@link TestRailSynContext} that is used when the plugin runs inside of gauge test run
 *
 * @author ajoecker
 */
public class TestRailSyncDefaultContext extends TestRailDefaultContext implements TestRailSynContext {

    @Override
    public String getGaugeTemplateId() {
        return System.getenv("testrail.gauge.template.id");
    }

    @Override
    public String getSpecFieldLabel() {
        return System.getenv("testrail.gauge.spec.label");
    }

    @Override
    public String getSectionId() {
        return System.getenv("testrail.section");
    }

    @Override
    public String getSpecLink() {
        return System.getenv("testrail.gauge.link");
    }
}
