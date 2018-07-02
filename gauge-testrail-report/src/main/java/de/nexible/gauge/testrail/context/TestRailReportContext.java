package de.nexible.gauge.testrail.context;

import de.nexible.gauge.testrail.config.TestRailContext;

import java.io.IOException;
import java.nio.file.Path;

public interface TestRailReportContext extends TestRailContext {
    String getTestRailRunId();

    void dump(Path output) throws IOException;
}
