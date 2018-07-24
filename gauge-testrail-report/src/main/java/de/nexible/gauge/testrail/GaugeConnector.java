package de.nexible.gauge.testrail;

import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Messages;
import com.thoughtworks.gauge.Spec;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static de.nexible.gauge.testrail.TestRailTimespanHandler.toTimeFormat;
import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;
import static java.lang.System.setOut;

/**
 * The {@link GaugeConnector} opens a socket to listen to gauge events and propagates the event
 *
 * @author ajoecker
 */
public class GaugeConnector {
    private static Logger logger = Logger.getLogger(GaugeConnector.class.getName());
    private static final String LOCALHOST = "127.0.0.1";

    private Socket socket;
    private List<GaugeResultListener> gaugeResultListeners = new ArrayList<>();
    private ConcurrentHashMap<String, Long> scenarioStartTime = new ConcurrentHashMap<>();

    void addGaugeResultListener(GaugeResultListener gaugeResultListener) {
        this.gaugeResultListeners.add(gaugeResultListener);
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
     * Listens to the socket and reacts on the suite execution result to propagate
     *
     * @throws IOException
     * @throws APIException
     */
    public void listen() throws IOException {
        if (socket == null) {
            logger.warning(() -> "no socket accessible");
            return;
        }

        try {
            while (!socket.isClosed() && socket.isConnected()) {
                Messages.Message message = Messages.Message.parseDelimitedFrom(socket.getInputStream());
                if (message.getMessageType() == Messages.Message.MessageType.ScenarioExecutionStarting) {
                    scenarioStartTime.put(message.getScenarioExecutionStartingRequest().getCurrentExecutionInfo().getCurrentScenario().getName(), System.currentTimeMillis());
                } else if (message.getMessageType() == Messages.Message.MessageType.ScenarioExecutionEnding) {
                    String stacktrace = message.getExecutionEndingRequest().getCurrentExecutionInfo().getStacktrace(); // this is empty
                    Spec.ProtoExecutionResult executionResult = message.getExecutionStatusResponse().getExecutionResult(); // this is empty
                    // executionResult would contain information about reason of failure or execution time
                    Messages.ScenarioInfo scenario = message.getScenarioExecutionEndingRequest().getCurrentExecutionInfo().getCurrentScenario();
                    boolean isFailed = scenario.getIsFailed(); // is the only information available

                } else if (message.getMessageType() == Messages.Message.MessageType.ExecutionStatusResponse) {
                    // this is never called
                } else if (message.getMessageType() == Messages.Message.MessageType.SuiteExecutionResult) {
                    return;
                }
            }
        } finally {
            socket.close();
        }
    }
}
