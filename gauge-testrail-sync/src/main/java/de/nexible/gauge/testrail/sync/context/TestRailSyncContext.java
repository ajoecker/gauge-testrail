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
    int getGaugeTemplateId();

    /**
     * Returns the database label of the spec field in TestRail
     *
     * @return
     */
    String getSpecFieldLabel();


    int projectId();

    /**
     * Returns the base link to the spec files in the gauge project
     *
     * @return
     */
    String getSpecLink();

    /**
     * Returns the gauge api port to listen to, to receive gauge data information
     *
     * @return
     */
    int getGaugeApiPort();
}
