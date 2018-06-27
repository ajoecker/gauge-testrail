package de.nexible.gauge.testrail;

import com.gurock.testrail.APIException;

import java.io.IOException;

public class GaugeTestRail {
    public static void main(String[] args) throws IOException, APIException {
        GaugeTestRailLogger.initializeLogger();
        GaugeConnector gaugeConnector = new GaugeConnector(new TestRailHandler(new TestRailContext()));
        gaugeConnector.connect();
        gaugeConnector.listen();
    }
}