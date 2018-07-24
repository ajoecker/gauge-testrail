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
}
