package de.nexible.gauge.testrail.context;

import de.nexible.gauge.testrail.config.GaugeDefaultContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.getenv;
import static java.nio.file.Paths.get;

/**
 * A {@link GaugeReportContext}, which is used per default when the plugin is run inside of gauge.
 *
 * @author ajoecker
 */
public class GaugeDefaultReportContext extends GaugeDefaultContext implements GaugeReportContext {
    private static final Logger logger = Logger.getLogger(GaugeDefaultReportContext.class.getName());

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

    private void ensureTestRailReportsDir() throws IOException {
        if (testRailReportsDir == null) {
            testRailReportsDir = get(getGaugeProjectRoot(), getGaugeReportsDir(), "testrail");
            Files.createDirectories(testRailReportsDir);
        }
    }
}
