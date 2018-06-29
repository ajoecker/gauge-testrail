package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.gauge.GaugeConnector;
import de.nexible.gauge.testrail.sync.gauge.GaugeStepRetriever;

import java.io.IOException;
import java.util.List;

public class TestRailSync {
    public static void main(String[] args) throws IOException {
        new TestRailSync().sync();
    }

    private void sync() throws IOException {
        GaugeConnector gaugeConnector = new GaugeConnector(new GaugeStepRetriever());
        List<Spec.ProtoSpec> connect = gaugeConnector.connect();
        System.out.println(connect);
    }
}
