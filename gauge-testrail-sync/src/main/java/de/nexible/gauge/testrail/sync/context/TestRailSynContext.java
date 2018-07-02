package de.nexible.gauge.testrail.sync.context;

import de.nexible.gauge.testrail.config.TestRailContext;

/**
 * A {@link TestRailContext} is the bridge to TestRail information
 *
 * @author ajoecker
 */
public interface TestRailSynContext extends TestRailContext {
    /**
     * Returns the id of the gauge template in TestRail
     *
     * @return
     */
    String getGaugeTemplateId();

    /**
     * Returns the database label of the spec field in TestRail
     *
     * @return
     */
    String getSpecFieldLabel();

    /**
     * Returns the id of the section, in which test cases will be added
     *
     * @return
     */
    String getSectionId();

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
