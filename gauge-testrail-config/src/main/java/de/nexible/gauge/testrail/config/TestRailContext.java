package de.nexible.gauge.testrail.config;

import com.gurock.testrail.APIClient;

import java.io.IOException;
import java.nio.file.Path;

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
    APIClient getTestRailClient();
}
