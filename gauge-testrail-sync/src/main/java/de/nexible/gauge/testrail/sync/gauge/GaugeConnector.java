package de.nexible.gauge.testrail.sync.gauge;

import com.thoughtworks.gauge.Spec;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class GaugeConnector {
    private static final String LOCALHOST = "127.0.0.1";
    private GaugeSpecRetriever gaugeSpecRetriever;

    public GaugeConnector(GaugeSpecRetriever gaugeSpecRetriever) {
        this.gaugeSpecRetriever = gaugeSpecRetriever;
    }

    public List<Spec.ProtoSpec> connect() throws IOException {
        Socket gaugeSocket = openSocket(Integer.parseInt(System.getenv("GAUGE_API_PORT")));
        try {
            return gaugeSpecRetriever.fetchAllSpecs(gaugeSocket);
        }
        finally {
            gaugeSocket.close();
        }
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
