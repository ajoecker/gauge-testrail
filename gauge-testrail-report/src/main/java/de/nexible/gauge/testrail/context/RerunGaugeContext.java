package de.nexible.gauge.testrail.context;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A {@link GaugeContext} that is used, when the plugin is rerun with a persisted gauge test suite result.
 *
 * @author ajoecker
 */
public class RerunGaugeContext implements GaugeContext {
    @Override
    public String getGaugeProjectRoot() {
        // the rerun is always run two levels below the gauge project
        return Paths.get(System.getProperty("user.dir")).getParent().getParent().toString();
    }

    @Override
    public boolean isRerun() {
        return true;
    }

    @Override
    public String getGaugeReportsDir() {
        return "reports";
    }

    @Override
    public String getGaugeLogDir() {
        return "logs";
    }

    @Override
    public Path getTestRailReportDir() {
        throw new UnsupportedOperationException("For a rerun the testrail report directory is not present");
    }
}
