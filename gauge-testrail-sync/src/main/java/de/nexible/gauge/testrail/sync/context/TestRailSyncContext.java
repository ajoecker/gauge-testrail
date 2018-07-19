package de.nexible.gauge.testrail.sync.context;

import de.nexible.gauge.testrail.config.TestRailContext;

/**
 * A {@link TestRailContext} is the bridge to TestRail information
 *
 * @author ajoecker
 */
public interface TestRailSyncContext extends TestRailContext {
    /**
     * Returns the id of the gauge template in TestRail
     *
     * @return
     */
    int getTemplateId();

    /**
     * Returns the id of the TestRail project
     *
     * @return
     */
    int projectId();

    /**
     * Returns the gauge api port to listen to, to receive gauge data information
     *
     * @return
     */
    int getGaugeApiPort();

    int getAutomationId();

    boolean isKnown(int automationId);
}
