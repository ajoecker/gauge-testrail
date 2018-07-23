package de.nexible.gauge.testrail.config;

import com.google.common.base.Strings;
import com.gurock.testrail.APIClient;

import java.io.IOException;
import java.nio.file.Path;
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
    APIClient getTestRailClient();

    boolean isDryRun();

    Level getLogLevel();

    default Level readLogLevel(String level) {
        if (!Strings.isNullOrEmpty(level)) {
            return Level.parse(level.trim());
        }
        return Level.INFO;
    };
}
