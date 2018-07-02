package de.nexible.gauge.testrail;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.context.GaugeReportContext;
import de.nexible.gauge.testrail.context.TestRailReportContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static de.nexible.gauge.testrail.RecoveryWriter.forUnix;
import static de.nexible.gauge.testrail.RecoveryWriter.forWindows;
import static java.lang.System.getProperty;
import static java.nio.file.Paths.get;

/**
 * The {@link GaugeLastRun} handles the rerun option of the plugin. It persists the result of a current run and allows to recover this result in a rerun
 *
 * @author ajoecker
 */
public class GaugeLastRun {
    private static final Logger logger = Logger.getLogger(GaugeLastRun.class.getName());
    private GaugeReportContext gaugeContext;
    private TestRailReportContext testRailContext;

    public GaugeLastRun(GaugeReportContext gaugeContext, TestRailReportContext testRailContext) {
        this.gaugeContext = gaugeContext;
        this.testRailContext = testRailContext;
    }

    /**
     * Persists the test results of a gauge run
     *
     * @param suiteResult
     * @throws IOException
     */
    public void persistRun(Spec.ProtoSuiteResult suiteResult) throws IOException {
        if (gaugeContext.isRerun()) {
            return;
        }
        Path lastRunFilePath = persistGaugeResult(suiteResult);
        Path pluginJar = get(getProperty("user.dir"), "bin", "testrail.jar");
        Path currentProperties = gaugeContext.getTestRailReportDir().resolve("testrail.properties").normalize();
        testRailContext.dump(currentProperties);
        createRecoveryFile(lastRunFilePath, pluginJar, currentProperties);
    }

    private void createRecoveryFile(Path lastRunFilePath, Path pluginJar, Path currentProperties) throws IOException {
        String command = "java -jar " + pluginJar.toString() + " -i " + lastRunFilePath.toString() + " -p " + currentProperties.toString();
        if (getProperty("os.name").contains("Windows")) {
            forWindows().withCommand(command).baseDirectory(gaugeContext.getTestRailReportDir()).write();
        } else {
            forUnix().withCommand(command).baseDirectory(gaugeContext.getTestRailReportDir()).write();
        }
    }

    private Path persistGaugeResult(Spec.ProtoSuiteResult suiteResult) throws IOException {
        Path lastRunFilePath = getLastRunFile();
        logger.info(() -> "persisting this run into " + lastRunFilePath);
        try (OutputStream os = Files.newOutputStream(lastRunFilePath)) {
            suiteResult.writeTo(os);
        }
        return lastRunFilePath;
    }

    private Path getLastRunFile() throws IOException {
        return gaugeContext.getTestRailReportDir().resolve("last_run.json").normalize();
    }

    /**
     * Recovers a previous run and loads test results from the persisted run file
     *
     * @return
     * @throws IOException
     */
    public Spec.ProtoSuiteResult recoverLastRun() throws IOException {
        Path lastRunFilePath = getLastRunFile();
        logger.info(() -> "recovering results from " + lastRunFilePath);
        try (InputStream ins = Files.newInputStream(lastRunFilePath)) {
            return Spec.ProtoSuiteResult.parseFrom(ins);
        }
    }
}
