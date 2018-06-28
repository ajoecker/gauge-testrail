package de.nexible.gauge.testrail;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes executables files for starting a rerun to post Gauge test results to TestRail.
 *
 * @author ajoecker
 */
public class RecoveryWriter {
    private final String suffix;
    private final String header;
    private Path testRailReportDir;
    private String command;

    private RecoveryWriter(String suffix, String header) {
        this.suffix = suffix;
        this.header = header;
    }

    /**
     * Creates a new {@link RecoveryWriter} for writing a <code>bat</code> file to rerun
     *
     * @return
     */
    public static RecoveryWriter forWindows() {
        return new RecoveryWriter(".bat", "@echo off");
    }

    /**
     * Creates a new {@link RecoveryWriter} for writing a <code>sh</code> file to rerun
     *
     * @return
     */
    public static RecoveryWriter forUnix() {
        return new RecoveryWriter(".sh", "#!/bin/bash");
    }

    public void write() throws IOException {
        Path recoverBat = testRailReportDir.resolve("testrail-recovery" + suffix).normalize();
        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(recoverBat))) {
            printWriter.println(header);
            printWriter.println(command);
        }
    }

    public RecoveryWriter baseDirectory(Path testRailReportDir) {
        this.testRailReportDir = testRailReportDir;
        return this;
    }

    public RecoveryWriter withCommand(String command) {
        this.command = command;
        return this;
    }
}
