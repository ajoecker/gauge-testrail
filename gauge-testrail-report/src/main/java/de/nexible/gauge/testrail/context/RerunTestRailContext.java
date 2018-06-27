package de.nexible.gauge.testrail.context;

import com.gurock.testrail.APIClient;

import java.util.Properties;
import java.util.logging.Logger;

public class RerunTestRailContext extends TestRailContext {
    private static final Logger logger = Logger.getLogger(RerunTestRailContext.class.getName());
    private final Properties properties;

    public RerunTestRailContext(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getTestRailRunId() {
        return properties.getProperty("testrail.run.id");
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
