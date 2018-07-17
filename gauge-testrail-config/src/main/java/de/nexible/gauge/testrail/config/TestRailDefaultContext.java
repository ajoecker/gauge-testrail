package de.nexible.gauge.testrail.config;

import com.gurock.testrail.APIClient;

import java.util.logging.Logger;

public class TestRailDefaultContext implements TestRailContext {
    private static final Logger logger = Logger.getLogger(TestRailDefaultContext.class.getName());

    @Override
    public APIClient getTestRailClient() {
        String url = System.getenv("testrail.url");
        String token = System.getenv("testrail.token");
        String user = System.getenv("testrail.user");
        logger.info(() -> "connecting to testrail instance " + url + " as " + user);
        APIClient client = new APIClient(url);
        client.setPassword(token);
        client.setUser(user);
        return client;
    }

    @Override
    public boolean isDryRun() {
        String dryRun = System.getenv("testrail.dryRun");
        if (!"".equals(dryRun)) {
           return Boolean.parseBoolean(dryRun.trim());
        }
        return false;
    }
}
