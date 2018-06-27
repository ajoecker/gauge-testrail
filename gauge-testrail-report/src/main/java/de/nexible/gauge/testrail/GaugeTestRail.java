package de.nexible.gauge.testrail;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.context.GaugeContext;
import de.nexible.gauge.testrail.context.RerunGaugeContext;
import de.nexible.gauge.testrail.context.RerunTestRailContext;
import de.nexible.gauge.testrail.context.TestRailContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class GaugeTestRail {
    @Parameter(names = {"--input", "-i"}, description = "path to the recovery file")
    private String lastRunFile = "";

    @Parameter(names = {"--properties", "-p"}, description = "path to testrail properties")
    private String testRailProperties = "";

    public static void main(String[] args) throws IOException, APIException {
        GaugeTestRail gaugeTestRail = new GaugeTestRail();
        JCommander.newBuilder().addObject(gaugeTestRail).build().parse(args);
        gaugeTestRail.run();
    }

    private void run() throws IOException, APIException {
        GaugeTestRailLogger.initializeLogger();
        GaugeContext gaugeContext = getGaugeContext();
        TestRailContext testRailContext = getTestRailContext();

        GaugeLastRun gaugeLastRun = new GaugeLastRun(gaugeContext);
        TestRailHandler testRailHandler = new TestRailHandler(testRailContext);

        if (gaugeContext.isRerun()) {
            testRailHandler.handle(gaugeLastRun.recoverLastRun());
        } else {
            GaugeConnector gaugeConnector = new GaugeConnector(testRailHandler, gaugeLastRun);
            gaugeConnector.connect();
            gaugeConnector.listen();
        }
    }

    private TestRailContext getTestRailContext() throws IOException {
        TestRailContext testRailContext;

        if (!"".equals(testRailProperties)) {
            Properties properties = new Properties();
            try (InputStream ins = Files.newInputStream(Paths.get(testRailProperties))) {
                properties.load(ins);
            }

            testRailContext = new RerunTestRailContext(properties);
        } else {
            testRailContext = new TestRailContext();
        }
        return testRailContext;
    }

    private GaugeContext getGaugeContext() {
        GaugeContext gaugeContext;

        if (!"".equals(lastRunFile)) {
            gaugeContext = new RerunGaugeContext();
        } else {
            gaugeContext = new GaugeContext();
        }
        return gaugeContext;
    }

}