package de.nexible.gauge.testrail.sync.gauge;

import com.thoughtworks.gauge.Spec;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class GaugeConnector {
    private static final String LOCALHOST = "127.0.0.1";
    private GaugeStepRetriever gaugeStepRetriever;

    public GaugeConnector(GaugeStepRetriever gaugeStepRetriever) {
        this.gaugeStepRetriever = gaugeStepRetriever;
    }

    public List<Spec.ProtoSpec> connect() throws IOException {
        Socket gaugeSocket = openSocket(Integer.parseInt(System.getenv("GAUGE_API_PORT")));
        List<Spec.ProtoSpec> protoSpecList = gaugeStepRetriever.fetchAllSteps(gaugeSocket);
        System.out.println(protoSpecList);
        gaugeSocket.close();
        return protoSpecList;
    }

    private static Socket openSocket(int port) {
        Socket clientSocket;
        while (true) {
            try {
                clientSocket = new Socket(LOCALHOST, port);
                break;
            } catch (Exception ignored) {
            }
        }

        return clientSocket;
    }
}
