package de.nexible.gauge.testrail.sync.gauge;

import com.thoughtworks.gauge.Spec;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;

/**
 * The {@link GaugeConnector} opens a socket to listen to gauge
 *
 * @author ajoecker
 */
public class GaugeConnector {
    private static final String LOCALHOST = "127.0.0.1";

    public static List<Spec.ProtoSpec> getSpecs(Function<Socket, List<Spec.ProtoSpec>> protoListener, int apiPort)  {
        try (Socket gaugeSocket = openSocket(apiPort)) {
            return protoListener.apply(gaugeSocket);
        }
        catch (IOException e) {
            // TODO logger
            return emptyList();
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
