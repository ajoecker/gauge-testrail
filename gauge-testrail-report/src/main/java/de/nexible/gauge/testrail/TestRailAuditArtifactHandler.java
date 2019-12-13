package de.nexible.gauge.testrail;

import com.google.protobuf.Descriptors;
import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Messages;
import de.nexible.gauge.testrail.config.TestRailUtil;
import de.nexible.gauge.testrail.context.TestRailReportContext;
import de.nexible.gauge.testrail.model.TestResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static de.nexible.gauge.testrail.model.TestResult.TestResultBuilder.newTestResult;

/**
 * A {@link TestRailAuditArtifactHandler} to upload test results from gauge to TestRail
 *
 * @author ajoecker
 */
public final class TestRailAuditArtifactHandler implements GaugeResultListener {
    private static final Logger logger = Logger.getLogger(TestRailAuditArtifactHandler.class.getName());
    // test-friendly
    static final String UPLOAD_TIME = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    private final TestRailReportContext testRailContext;
    private static final HashMap<String, List<String>> unannotatedTestsByScenario = new HashMap<>();
    // todo: Change to real build test result path etc
    private static final String UNANNOTATED_TEST_ARTIFACT_PATH = "./tmp/test_results/unannotated_tests.json";

    /**
     @implNote this nice to have git diff is arguably more useful if we are able to get the file names of each scenario we have
     modified, thereby only alerting dev/qe to the pertinent tests to tag with test case ids from testrail
     HOWEVER!? if the scenarioInfo.getDescriptorForType().getFile() doesn't work for some reason,
     setting this boolean to false will yield simply all scenarios not tagged. Better than nothing s( ^‸^)-p
     */
    private static final boolean IS_FILE_PATH_FOR_SCENARIOS_TOTALLY_BOGUS = false;

    public TestRailAuditArtifactHandler(TestRailReportContext testRailContext) {
        this.testRailContext = testRailContext;
    }

    /**
     * this method is the hand off for the post to the testrail client when sending run results for a scenario
     * there can be multiple scenarios ran in a single run, so the artifact should be a static hash of:
     *   HashSet<String, List<String>> or something to help capture tests / scenarios that are not tagged appropriatelyz
     */
    @Override
    public void gaugeResult(Messages.ScenarioInfo scenarioInfo, String executionTime) {
        String testRailRunId = testRailContext.getTestRailRunId();

        try {
            addUnannotatedTests(scenarioInfo);
        } catch (Exception e) {
            logger.warning(() -> "Unable to add unannotated tests" + e.getMessage() + ":\n " + e.getStackTrace());
        }

        if (isNullOrEmpty(testRailRunId)) {
            logger.warning(() -> "No testrail run id given. No results are posted to TestRail.");
        } else {
            logger.info(() -> "handling gauge result for a total scenarios: " + scenarioInfo.getName());
            convertToJson(scenarioInfo, executionTime)
                    .ifPresentOrElse(
                            data -> send(testRailRunId, data),
                            () -> logger.warning(() -> "no test results found or none is tagged with a testrail case id. " +
                                    "No results are posted to TestRail"));
        }

        try {
            saveUnannotatedTestArtifact();
        } catch (IOException e) {
            logger.warning(() -> "Unable to save unannotated tests" + e.getMessage() + ":\n " + e.getStackTrace());
        }
    }

    private Set<String> buildModifiedSpecListForScenario(Messages.ScenarioInfo scenarioInfo) throws IOException {
        if (IS_FILE_PATH_FOR_SCENARIOS_TOTALLY_BOGUS) {
            return new HashSet<String>();
        }

        // from the parallel ruby code:
        //        changed_spec_files = `git diff --name-status master | grep "spec.rb"`.strip.split(/\n/)
        //          .select { |line| line[0] == 'M' || line[0] == 'A' }
        //          .map { |line| line.split(/\t/)[1] }

        String file = scenarioInfo.getDescriptorForType().getFile().getName();
        List<String> list = new ArrayList<String>();

        Process diffProcess = Runtime.getRuntime().exec("git diff --name-status master");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        }

        // we just want the spec files that were modified or added, and only those that match the current spec
        return list.stream().filter(f -> f.contains(".spec") && (f.charAt(0) == 'M' || f.charAt(0) == 'A'))
                            .map(f -> f.split("\t")[1])
                            .filter(f -> f.contains(file))
                                .collect(Collectors.toSet());
    }

    private synchronized void addUnannotatedTests(Messages.ScenarioInfo scenarioInfo) throws IOException {
        final Set<String> modifiedSpecSet = buildModifiedSpecListForScenario(scenarioInfo);

        List<String> unnanotatedTests = scenarioInfo.getTagsList().stream()
                                                    .filter(id -> !TestRailUtil.isTestRailTag(id))
                                                        .collect(Collectors.toList());
        if (!IS_FILE_PATH_FOR_SCENARIOS_TOTALLY_BOGUS && modifiedSpecSet.isEmpty()) {
            // we didn't modify this spec this run, or the file path for scenarios is bogus :(
            return;
        }

        if (unannotatedTestsByScenario.containsKey(scenarioInfo.getName())) {
            unannotatedTestsByScenario.get(scenarioInfo.getName()).addAll(unnanotatedTests);
        } else {
            unannotatedTestsByScenario.put(scenarioInfo.getName(), unnanotatedTests);
        }
    }

    /**
     * @implNote this is kind of lazy, however as gauge tests can be multithreaded, we should synchronize this method
     *   to prevent any IO mishaps
     * @throws IOException
     */
    private synchronized void saveUnannotatedTestArtifact() throws IOException {
        JSONObject json = new JSONObject();
        JSONObject testsByScenario = new JSONObject();
        for (Map.Entry<String, List<String>> entry : unannotatedTestsByScenario.entrySet()) {
            testsByScenario.put(entry.getKey(), entry.getValue().stream().collect(JSONArray::new, JSONArray::add, (x, y) -> {}));
        }

        json.put("unannotated-tests", testsByScenario);
        try (FileWriter writer = new FileWriter(UNANNOTATED_TEST_ARTIFACT_PATH)) {
            json.writeJSONString(writer);
        }
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
