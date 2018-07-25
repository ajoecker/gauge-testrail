package de.nexible.gauge.testrail.config;

import com.gurock.testrail.TestRailClient;

import java.util.logging.Level;

/**
 * A {@link TestRailContext} is the bridge to TestRail information
 *
 * @author ajoecker
 */
public interface TestRailContext {
    /**
     * Returns the client for TestRail interactions
     *
     * @return
     */
    TestRailClient getTestRailClient();

    /**
     * Returns whether no data shall be submitted or changed during a run
     *
     * @return
     */
    boolean isDryRun();

    Level getLogLevel();
}
