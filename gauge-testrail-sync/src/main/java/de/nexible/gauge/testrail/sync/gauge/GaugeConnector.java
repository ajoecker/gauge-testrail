package de.nexible.gauge.testrail.sync.gauge;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.context.TestRailSynContext;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * The {@link GaugeConnector} opens a socket to listen to gauge
 *
 * @author ajoecker
 */
public class GaugeConnector {
    private static final String LOCALHOST = "127.0.0.1";
    private GaugeSpecRetriever gaugeSpecRetriever;
    private TestRailSynContext testRailSynContext;

    public GaugeConnector(GaugeSpecRetriever gaugeSpecRetriever, TestRailSynContext testRailSynContext) {
        this.gaugeSpecRetriever = gaugeSpecRetriever;
        this.testRailSynContext = testRailSynContext;
    }

    public List<Spec.ProtoSpec> getSpecs() throws IOException {
        try (Socket gaugeSocket = openSocket(testRailSynContext.getGaugeApiPort())) {
            return gaugeSpecRetriever.fetchAllSpecs(gaugeSocket);
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
