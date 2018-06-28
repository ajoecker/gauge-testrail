package de.nexible.gauge.testrail.context;

import sun.rmi.runtime.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.getenv;
import static java.nio.file.Paths.get;

/**
 * A {@link GaugeContext}, which is used per default when the plugin is run inside of gauge.
 *
 * @author ajoecker
 */
public class GaugeDefaultContext implements GaugeContext {
    private static final Logger logger = Logger.getLogger(GaugeDefaultContext.class.getName());

    private Path testRailReportsDir;

    public Path getTestRailReportDir() {
        try {
            ensureTestRailReportsDir();
        } catch (IOException e) {
            logger.log(Level.WARNING, e, () -> "Failed to create TestRail report dir");
        }
        return testRailReportsDir;
    }

    public boolean isRerun() {
        return false;
    }

    public String getGaugeProjectRoot() {
        return getenv("GAUGE_PROJECT_ROOT");
    }

    public String getGaugeReportsDir() {
        return getenv("gauge_reports_dir");
    }

    public String getGaugeLogDir() {
        return System.getenv("logs_directory");
    }

    private void ensureTestRailReportsDir() throws IOException {
        if (testRailReportsDir == null) {
            testRailReportsDir = get(getGaugeProjectRoot(), getGaugeReportsDir(), "testrail");
            Files.createDirectories(testRailReportsDir);
        }
    }
}
