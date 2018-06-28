package de.nexible.gauge.testrail.context;

import com.gurock.testrail.APIClient;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * A {@link TestRailContext} that is used when the plugin runs inside of gauge test run
 *
 * @author ajoecker
 */
public class TestRailDefaultContext implements TestRailContext {
    private static final Logger logger = Logger.getLogger(TestRailDefaultContext.class.getName());

    @Override
    public APIClient getTestRailClient() {
        String url = System.getenv("testrail.url");
        String token = System.getenv("testrail.token");
        String user = System.getenv("testrail.user");
        logger.info(() -> "connecting to testrail instance " + url + " as " + user + " / " + token);
        APIClient client = new APIClient(url);
        client.setPassword(token);
        client.setUser(user);
        return client;
    }

    @Override
    public String getTestRailRunId() {
        return System.getenv("testrail.run.id");
    }

    @Override
    public void dump(Path output) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(output))) {
            pw.println("testrail.url = " + System.getenv("testrail.url"));
            pw.println("testrail.token = " + System.getenv("testrail.token"));
            pw.println("testrail.user = " + System.getenv("testrail.user"));
            pw.println("testrail.run.id = " + System.getenv("testrail.run.id"));
        }
    }
}
