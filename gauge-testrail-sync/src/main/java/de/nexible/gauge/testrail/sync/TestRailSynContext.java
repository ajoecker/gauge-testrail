package de.nexible.gauge.testrail.sync;

import de.nexible.gauge.testrail.config.TestRailContext;

/**
 * A {@link TestRailContext} is the bridge to TestRail information
 *
 * @author ajoecker
 */
public interface TestRailSynContext extends TestRailContext {
    String getGaugeTemplateId();
    String getSpecFieldLabel();
    String getSectionId();
    String getSpecLink();
}
