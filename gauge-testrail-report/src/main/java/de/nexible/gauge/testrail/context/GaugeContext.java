package de.nexible.gauge.testrail.context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.getenv;
import static java.nio.file.Paths.get;

public class GaugeContext {
    private Path testRailReportsDir;

    public Path getTestRailReportDir() throws IOException {
        ensureTestRailReportsDir();
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

    private void ensureTestRailReportsDir() throws IOException {
        if (testRailReportsDir == null) {
            testRailReportsDir = get(getGaugeProjectRoot(), getGaugeReportsDir(), "testrail");
            Files.createDirectories(testRailReportsDir);
        }
    }
}
