package de.nexible.gauge.testrail.context;

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

    /**
     * Returns the test run id for which results shall be posted
     *
     * @return
     */
    String getTestRailRunId();

    /**
     * Dumps all information of the context as properties into the given output.
     *
     * @param output
     * @throws IOException
     */
    void dump(Path output) throws IOException;
}
