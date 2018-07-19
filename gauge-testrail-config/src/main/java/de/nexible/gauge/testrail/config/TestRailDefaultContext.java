package de.nexible.gauge.testrail.config;

import com.gurock.testrail.APIClient;

import java.util.logging.Logger;

public class TestRailDefaultContext implements TestRailContext {
    private static final Logger logger = Logger.getLogger(TestRailDefaultContext.class.getName());

    @Override
    public APIClient getTestRailClient() {
        String url = read("testrail.url");
        String token = read("testrail.token");
        String user = read("testrail.user");
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

    private String read(String key) {
        return System.getenv(key).trim();
    }
}
