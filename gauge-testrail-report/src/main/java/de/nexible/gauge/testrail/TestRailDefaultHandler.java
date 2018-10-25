package de.nexible.gauge.testrail;

import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Messages;
import de.nexible.gauge.testrail.config.TestRailUtil;
import de.nexible.gauge.testrail.context.TestRailReportContext;
import de.nexible.gauge.testrail.model.TestResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static de.nexible.gauge.testrail.model.TestResult.TestResultBuilder.newTestResult;

/**
 * A {@link TestRailDefaultHandler} to upload test results from gauge to TestRail
 *
 * @author ajoecker
 */
public final class TestRailDefaultHandler implements GaugeResultListener {
    private static final Logger logger = Logger.getLogger(TestRailDefaultHandler.class.getName());
    // test-friendly
    static final String UPLOAD_TIME = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    private final TestRailReportContext testRailContext;

    public TestRailDefaultHandler(TestRailReportContext testRailContext) {
        this.testRailContext = testRailContext;
    }

    @Override
    public void gaugeResult(Messages.ScenarioInfo scenarioInfo, String executionTime) {
        String testRailRunId = testRailContext.getTestRailRunId();

        if (isNullOrEmpty(testRailRunId)) {
            logger.warning(() -> "No testrail run id given. No results are posted to TestRail.");
            return;
        }

        logger.info(() -> "handling gauge result for a total scenarios: " + scenarioInfo.getName());
        convertToJson(scenarioInfo, executionTime)
                .ifPresentOrElse(
                        data -> send(testRailRunId, data),
                        () -> logger.warning(() -> "no test results found or none is tagged with a testrail case id. " +
                                "No results are posted to TestRail"));
    }

    private void send(String testRailRunId, JSONObject jsonObject) {
        logger.info(() -> "sending results to testrail for run #" + testRailRunId);
        if (!testRailContext.isDryRun()) {
            try {
                testRailContext.getTestRailClient().addResult(testRailRunId, jsonObject);
            } catch (IOException | APIException e) {
                logger.info(() -> "could not send " + jsonObject + " to run " + testRailRunId);
                logger.log(Level.WARNING, e, () -> "Failed to send to TestRail: " + e.getMessage());
            }
            logger.info(() -> "results have been sent");
        } else {
            logger.info(() -> "send to TestRail: " + jsonObject);
            logger.info(() -> "dry run started, so no results are posted");
        }
    }

    private Optional<JSONObject> convertToJson(Messages.ScenarioInfo scenarioInfo, String executionTime) {
        List<TestResult> testResult = createTestResult(scenarioInfo, executionTime);
        JSONArray collect = testResult.stream().map(TestResult::toJsonObject).collect(JSONArray::new, JSONArray::add, (x, y) -> {
        });

        if (!collect.isEmpty()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("results", collect);
            return Optional.of(jsonObject);
        }
        return Optional.empty();
    }

    private List<TestResult> createTestResult(Messages.ScenarioInfo scenarioInfo, String executionTime) {
        int status = scenarioInfo.getIsFailed() ? 5 : 1;
        return scenarioInfo.getTagsList().stream().filter(TestRailUtil::isTestRailTag).map(id -> toTestResult(status, id, executionTime)).collect(Collectors.toList());
    }

    private TestResult toTestResult(int status, String caseId, String executionTime) {
        return newTestResult().forCase(caseId.substring(1)).withStatus(status).elapsed(executionTime).comment(buildComment()).done();
    }

    private String buildComment() {
        return "Test executed automatically uploaded by Gauge at " + UPLOAD_TIME;
    }
}
