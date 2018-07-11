package de.nexible.gauge.testrail;

import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.config.TestRailConfig;
import de.nexible.gauge.testrail.context.TestRailReportContext;
import de.nexible.gauge.testrail.model.TestResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.nexible.gauge.testrail.TestRailTimespanHandler.toTimeFormat;
import static de.nexible.gauge.testrail.model.TestResult.TestResultBuilder.newTestResult;

/**
 * A {@link TestRailHandler} to upload test results from gauge to TestRail
 *
 * @author ajoecker
 */
public final class TestRailDefaultHandler implements GaugeResultListener {
    private static final Logger logger = Logger.getLogger(TestRailDefaultHandler.class.getName());
    // test-friendly
    static final String UPLOAD_TIME = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    private static final int IGNORE = 0;
    private final TestRailReportContext testRailContext;

    public TestRailDefaultHandler(TestRailReportContext testRailContext) {
        this.testRailContext = testRailContext;
    }

    private List<Spec.ProtoScenario> retrieveScenarios(Spec.ProtoSuiteResult suiteResult) {
        return suiteResult.getSpecResultsList().stream()
                .map(Spec.ProtoSpecResult::getProtoSpec)
                .flatMap(ps -> ps.getItemsList().stream())
                .filter(protoItem -> protoItem.getItemType() == Spec.ProtoItem.ItemType.Scenario)
                .map(Spec.ProtoItem::getScenario).collect(Collectors.toList());
    }

    @Override
    public void gaugeResult(Spec.ProtoSuiteResult suiteResult) {
        List<Spec.ProtoScenario> scenarios = retrieveScenarios(suiteResult);
        String testRailRunId = testRailContext.getTestRailRunId();

        if (testRailRunId == null || "".equals(testRailRunId)) {
            logger.warning(() -> "No testrail run id given. No results are posted to TestRail.");
            return;
        }

        logger.info(() -> "handling gauge result for a total scenarios: " + scenarios.size());
        JSONObject jsonObject = convertToJson(scenarios);
        if (jsonObject.isEmpty()) {
            logger.warning(() -> "no test results found or none is tagged with a testrail case id. No results are posted to TestRail");
            return;
        }
        logger.info(() -> "sending results to testrail for run #" + testRailRunId);
        if (!testRailContext.isDryRun()) {
            try {
                testRailContext.getTestRailClient().sendPost("add_results_for_cases/" + testRailRunId, jsonObject);
            } catch (IOException | APIException e) {
                logger.log(Level.WARNING, e, () -> "Failed to send to TestRail.");
            }
            logger.info(() -> "results have been sent");
        } else {
            logger.info(() -> "send to TestRail: " + jsonObject);
            logger.info(() -> "dry run started, so no results are posted");
        }
    }

    private JSONObject convertToJson(List<Spec.ProtoScenario> scenarios) {
        JSONObject jsonObject = new JSONObject();
        JSONArray a = scenarios.stream()
                .flatMap(scenario -> createTestResult(scenario).stream())
                .map(TestResult::toJsonObject)
                .collect(JSONArray::new, JSONArray::add, (x, y) -> {
                });

        if (!a.isEmpty()) {
            jsonObject.put("results", a);
        }
        return jsonObject;
    }

    private List<TestResult> createTestResult(Spec.ProtoScenario protoScenario) {
        int status = mapStatusToTestRail(protoScenario.getExecutionStatus());
        if (status == IGNORE) {
            return Collections.emptyList();
        }

        return getTestRailCaseIds(protoScenario).stream()
                .map(caseId -> toTestResult(protoScenario, status, caseId))
                .collect(Collectors.toList());
    }

    private TestResult toTestResult(Spec.ProtoScenario protoScenario, int status, String caseId) {
        return newTestResult().forCase(caseId).withStatus(status).elapsed(toTimeFormat(protoScenario.getExecutionTime())).comment(buildComment(protoScenario)).done();
    }

    private List<String> getTestRailCaseIds(Spec.ProtoScenario protoScenario) {
        return protoScenario.getTagsList().stream()
                .filter(TestRailConfig::isTestRailTag)
                .map(id -> id.substring(1))
                .collect(Collectors.toList());
    }

    private String buildComment(Spec.ProtoScenario scenario) {
        return "Test executed automatically uploaded by Gauge at " + UPLOAD_TIME
                + ". Testcase" + scenario.getID() + ": '" + scenario.getScenarioHeading() + "'";
    }

    private int mapStatusToTestRail(Spec.ExecutionStatus executionStatus) {
        switch (executionStatus) {
            case FAILED:
                return 5; // 5 = FAILED in TestRail
            case PASSED:
                return 1; // 1 = PASSED in TestRail
            case SKIPPED:
            case NOTEXECUTED:
            case UNRECOGNIZED:
            default:
                return IGNORE; // rest is ignored currently
        }
    }
}
