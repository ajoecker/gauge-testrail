package de.nexible.gauge.testrail.config;

import com.google.common.base.Strings;
import com.gurock.testrail.TestRailClient;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestRailDefaultContext implements TestRailContext {
    private static final Logger logger = Logger.getLogger(TestRailDefaultContext.class.getName());

    @Override
    public TestRailClient getTestRailClient() {
        String url = read("testrail.url");
        String token = read("testrail.token");
        String user = read("testrail.user");
        logger.info(() -> "connecting to testrail instance " + url + " as " + user);
        return TestRailClient.newClient(url, user, token);
    }

    @Override
    public boolean isDryRun() {
        return readBoolean("testrail.dryRun");
    }

    protected boolean readBoolean(String key) {
        String value = Environment.get(key);
        if (!Strings.isNullOrEmpty(value)) {
            return Boolean.parseBoolean(value.trim());
        }
        return false;
    }

    private String read(String key) {
        return Environment.get(key).trim();
    }

    @Override
    public Level getLogLevel() {
        return readLogLevel(Environment.get("testrail.loglevel"));
    }

    private Level readLogLevel(String level) {
        if (!Strings.isNullOrEmpty(level)) {
            return Level.parse(level.trim());
        }
        return Level.INFO;
    }
}
