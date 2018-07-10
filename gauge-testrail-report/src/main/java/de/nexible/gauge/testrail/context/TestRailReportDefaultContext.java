package de.nexible.gauge.testrail.context;

import de.nexible.gauge.testrail.config.TestRailContext;
import de.nexible.gauge.testrail.config.TestRailDefaultContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link TestRailContext} that is used when the plugin runs inside of gauge test run
 *
 * @author ajoecker
 */
public class TestRailReportDefaultContext extends TestRailDefaultContext implements TestRailReportContext {
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
