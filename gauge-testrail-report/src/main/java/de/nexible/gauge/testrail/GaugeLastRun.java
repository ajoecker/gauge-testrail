package de.nexible.gauge.testrail;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.context.GaugeContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class GaugeLastRun {
    private static final Logger logger = Logger.getLogger(GaugeLastRun.class.getName());
    private GaugeContext gaugeContext;

    public GaugeLastRun(GaugeContext gaugeContext) {
        this.gaugeContext = gaugeContext;
    }

    public void persistRun(Spec.ProtoSuiteResult suiteResult) throws IOException {
        if (gaugeContext.isRerun()) {
            return;
        }
        Path lastRunFilePath = getLastRunFile();
        logger.info(() -> "persisting this run into " + lastRunFilePath);
        try (OutputStream os = Files.newOutputStream(lastRunFilePath)) {
            suiteResult.writeTo(os);
        }
        Path pluginJar = Paths.get(System.getProperty("user.dir"), "bin", "testrail.jar");
        if (System.getProperty("os.name").contains("Windows")) {
            Path recoverSh = gaugeContext.getTestRailReportDir().resolve("testrail-recover" + ".bat").normalize();
            try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(recoverSh))) {
                printWriter.println("@echo off");
                printWriter.println("java -jar " + pluginJar.toString());
            }
        } else {
            Path recoverSh = gaugeContext.getTestRailReportDir().resolve("testrail-recover" + ".sh").normalize();
            try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(recoverSh))) {
                printWriter.println("#!/bin/bash");
                printWriter.println("java -jar " + pluginJar.toString());
            }
        }
    }

    private Path getLastRunFile() throws IOException {
        return gaugeContext.getTestRailReportDir().resolve("last_run.json").normalize();
    }

    public Spec.ProtoSuiteResult recoverLastRun() throws IOException {
        Path lastRunFilePath = getLastRunFile();
        logger.info(() -> "recovering results from " + lastRunFilePath);
        try (InputStream ins = Files.newInputStream(lastRunFilePath)) {
            return Spec.ProtoSuiteResult.parseFrom(ins);
        }
    }
}
