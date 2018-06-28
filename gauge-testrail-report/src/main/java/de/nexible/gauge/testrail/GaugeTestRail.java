package de.nexible.gauge.testrail;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.context.GaugeContext;
import de.nexible.gauge.testrail.context.GaugeDefaultContext;
import de.nexible.gauge.testrail.context.RerunGaugeContext;
import de.nexible.gauge.testrail.context.RerunTestRailContext;
import de.nexible.gauge.testrail.context.TestRailContext;
import de.nexible.gauge.testrail.context.TestRailDefaultContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Accesspoint for the plugin.
 * <p>
 * When started from Gauge, the {@link GaugeConnector} is responsible for listenting and reacting on gauge events.
 * When started from command line (as rerun option), it requires two parameters
 * <ol>
 * <li>-i: path to the recovery file, the persisted gauge results from a previous run</li>
 * <li>-p: path to the testrail.properties used for the previous run (e.g. <code>-gauge.root-/env/default/testrailproperties</code></li>
 * </ol>
 *
 * @author ajoecker
 */
public class GaugeTestRail {
    @Parameter(names = {"--input", "-i"}, description = "path to the recovery file. Required for rerun option")
    private String lastRunFile = "";

    @Parameter(names = {"--properties", "-p"}, description = "path to testrail properties. Required for rerun option")
    private String testRailProperties = "";

    public static void main(String[] args) throws IOException, APIException {
        GaugeTestRail gaugeTestRail = new GaugeTestRail();
        JCommander.newBuilder().addObject(gaugeTestRail).build().parse(args);
        gaugeTestRail.run();
    }

    private void run() throws IOException, APIException {
        GaugeContext gaugeContext = getGaugeContext();
        GaugeTestRailLogger.initializeLogger(gaugeContext);
        TestRailContext testRailContext = getTestRailContext();

        GaugeLastRun gaugeLastRun = new GaugeLastRun(gaugeContext, testRailContext);
        TestRailHandler testRailHandler = new TestRailDefaultHandler(testRailContext);

        if (gaugeContext.isRerun()) {
            testRailHandler.handle(gaugeLastRun.recoverLastRun());
        } else {
            GaugeConnector gaugeConnector = new GaugeConnector(testRailHandler, gaugeLastRun);
            gaugeConnector.connect();
            gaugeConnector.listen();
        }
    }

    private TestRailContext getTestRailContext() throws IOException {
        if (!"".equals(testRailProperties)) {
            Properties properties = new Properties();
            try (InputStream ins = Files.newInputStream(Paths.get(testRailProperties))) {
                properties.load(ins);
            }
            return new RerunTestRailContext(properties);
        } else {
            return new TestRailDefaultContext();
        }
    }

    private GaugeContext getGaugeContext() {
        if (!"".equals(lastRunFile)) {
            return new RerunGaugeContext();
        } else {
            return new GaugeDefaultContext();
        }
    }

}