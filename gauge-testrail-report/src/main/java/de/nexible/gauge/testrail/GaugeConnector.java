package de.nexible.gauge.testrail;

import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Messages;
import com.thoughtworks.gauge.Spec;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.System.getenv;

public class GaugeConnector {
    private static Logger logger = Logger.getLogger(GaugeConnector.class.getName());
    private static final String LOCALHOST = "127.0.0.1";

    private final TestRailHandler testRailHandler;
    private Socket socket;

    public GaugeConnector(TestRailHandler testRailHandler) {
        this.testRailHandler = testRailHandler;
    }

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
        persistRun(suiteResult);
        testRailHandler.handle(retrieveScenarios(suiteResult));
    }

    private void persistRun(Spec.ProtoSuiteResult suiteResult) throws IOException {
        String root = System.getenv("GAUGE_PROJECT_ROOT");
        String reportsDir = System.getenv("gauge_reports_dir");
        Path dest = Paths.get(root, reportsDir, "testrail");
        Files.createDirectories(dest);

        Path lastRunFilePath = dest.resolve("last_run.json").normalize();
        logger.info(() -> "persisting this run into " + lastRunFilePath);
        Files.write(lastRunFilePath, suiteResult.toString().getBytes());
    }

    private List<Spec.ProtoScenario> retrieveScenarios(Spec.ProtoSuiteResult suiteResult) {
        return suiteResult.getSpecResultsList().stream()
                .map(Spec.ProtoSpecResult::getProtoSpec)
                .flatMap(ps -> ps.getItemsList().stream())
                .filter(protoItem -> protoItem.getItemType() == Spec.ProtoItem.ItemType.Scenario)
                .map(Spec.ProtoItem::getScenario).collect(Collectors.toList());
    }
}
