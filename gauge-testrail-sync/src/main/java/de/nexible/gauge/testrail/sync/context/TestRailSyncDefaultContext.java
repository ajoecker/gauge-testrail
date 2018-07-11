package de.nexible.gauge.testrail.sync.context;

import de.nexible.gauge.testrail.config.TestRailDefaultContext;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;

/**
 * A {@link TestRailSyncContext} that is used when the plugin runs inside of gauge test run
 *
 * @author ajoecker
 */
public class TestRailSyncDefaultContext extends TestRailDefaultContext implements TestRailSyncContext {
    @Override
    public int getGaugeApiPort() {
        return parseInt(getenv("GAUGE_API_PORT"));
    }

    @Override
    public int projectId() {
        return parseInt(getenv("testrail.project"));
    }

    @Override
    public String getGaugeTemplateId() {
        return getenv("testrail.gauge.template.id");
    }

    @Override
    public String getSpecFieldLabel() {
        return getenv("testrail.gauge.spec.label");
    }

    @Override
    public String getSectionId() {
        return getenv("testrail.section");
    }

    @Override
    public String getSpecLink() {
        return getenv("testrail.gauge.link");
    }
}
