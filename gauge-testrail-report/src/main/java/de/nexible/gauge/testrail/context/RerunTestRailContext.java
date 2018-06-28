package de.nexible.gauge.testrail.context;

import com.gurock.testrail.APIClient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A {@link TestRailContext} that is used when the plugin is rerun with a presisted gauge suite result
 *
 * @author ajoecker
 */
public class RerunTestRailContext implements TestRailContext {
    private static final Logger logger = Logger.getLogger(RerunTestRailContext.class.getName());
    private final Properties properties;

    public RerunTestRailContext(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getTestRailRunId() {
        return properties.getProperty("testrail.run.id");
    }

    @Override
    public void dump(Path output) throws IOException {
        throw new UnsupportedOperationException("For a rerun no dumping is required and not supported");
    }

    public APIClient getTestRailClient() {
        String url = properties.getProperty("testrail.url");
        String token = properties.getProperty("testrail.token");
        String user = properties.getProperty("testrail.user");
        logger.info(() -> "connecting to testrail instance " + url + " as " + user + " / " + token);
        APIClient client = new APIClient(url);
        client.setPassword(token);
        client.setUser(user);
        return client;
    }
}
