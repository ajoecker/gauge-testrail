package de.nexible.gauge.testrail;

import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Messages;
import com.thoughtworks.gauge.Spec;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;

/**
 * The {@link GaugeConnector} opens a socket to listen to gauge events and propagates the event SuiteExecutionResult to {@link TestRailHandler}
 *
 * @author ajoecker
 */
public class GaugeConnector {
    private static Logger logger = Logger.getLogger(GaugeConnector.class.getName());
    private static final String LOCALHOST = "127.0.0.1";

    private final TestRailHandler testRailHandler;
    private final GaugeLastRun gaugeLastRun;
    private Socket socket;

    public GaugeConnector(TestRailHandler testRailHandler, GaugeLastRun gaugeLastRun) {
        this.testRailHandler = testRailHandler;
        this.gaugeLastRun = gaugeLastRun;
    }

    /**
     * Opens a socket to listen to gauge events
     */
    public void connect() {
        int port = parseInt(getenv("plugin_connection_port"));
        logger.info(() -> "connecting to gauge port " + port);
        while (true) {
            try {
                this.socket = new Socket(LOCALHOST, port);
                logger.info(() -> "connected to socket " + LOCALHOST + ":" + port);
                break;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Listens to the socket and reacts on the suite execution result to propagate this to {@link TestRailHandler}
     *
     * @throws IOException
     * @throws APIException
     */
    public void listen() throws IOException, APIException {
        if (socket == null) {
            logger.warning(() -> "no socket accessible");
            return;
        }

        try {
            while (!socket.isClosed() && socket.isConnected()) {
                Messages.Message message = Messages.Message.parseDelimitedFrom(socket.getInputStream());
                if (message.getMessageType() == Messages.Message.MessageType.SuiteExecutionResult) {
                    handleSuiteResult(message);
                    return;
                }
            }
        } finally {
            socket.close();
        }
    }

    private void handleSuiteResult(Messages.Message message) throws IOException, APIException {
        logger.info(() -> "retrieved suite execution result message");
        Messages.SuiteExecutionResult executionResult = message.getSuiteExecutionResult();
        Spec.ProtoSuiteResult suiteResult = executionResult.getSuiteResult();
        gaugeLastRun.persistRun(suiteResult);
        testRailHandler.handle(suiteResult);
    }
}
