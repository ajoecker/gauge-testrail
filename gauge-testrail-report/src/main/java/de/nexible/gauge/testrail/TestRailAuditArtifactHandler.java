package de.nexible.gauge.testrail;

import com.thoughtworks.gauge.Messages;
import de.nexible.gauge.testrail.config.TestRailUtil;
import de.nexible.gauge.testrail.context.TestRailReportContext;
import de.nexible.gauge.testrail.services.AuditArtifactModelStorageService;
import de.nexible.gauge.testrail.services.AuditArtifactWriterService;
import de.nexible.gauge.testrail.services.PRSpecFileNameDiffService;
import de.nexible.gauge.testrail.services.ScenarioInfoProxyService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A {@link TestRailAuditArtifactHandler} to upload test results from gauge to TestRail
 *
 * @author ajoecker
 */
public class TestRailAuditArtifactHandler implements GaugeResultListener {
    private static final Logger logger = Logger.getLogger(TestRailAuditArtifactHandler.class.getName());

    private final TestRailReportContext testRailContext;
    private final PRSpecFileNameDiffService prFileNameDiffService;
    private final AuditArtifactWriterService auditArtifactWriterService;
    private final ScenarioInfoProxyService scenarioInfoProxyService;
    private final AuditArtifactModelStorageService auditArtifactModelStorageService;

    // todo: Change to real build test result path etc
    private static final String UNANNOTATED_TEST_ARTIFACT_PATH = "./tmp/test_results/unannotated_tests.json";

    /**
     @implNote this nice to have git diff is arguably more useful if we are able to get the file names of each scenario we have
     modified, thereby only alerting dev/qe to the pertinent tests to tag with test case ids from testrail
     HOWEVER!? if the scenarioInfo.getDescriptorForType().getFile() doesn't work for some reason,
     setting this boolean to false will yield simply all scenarios not tagged. Better than nothing s( ^‸^)-p
     */
    private static final boolean IS_FILE_PATH_FOR_SCENARIOS_TOTALLY_BOGUS = false;

    public TestRailAuditArtifactHandler(TestRailReportContext testRailContext,
                                        PRSpecFileNameDiffService prFileNameDiffService,
                                        AuditArtifactWriterService auditArtifactWriterService,
                                        ScenarioInfoProxyService scenarioInfoProxyService,
                                        AuditArtifactModelStorageService auditArtifactModelStorageService) {
        this.testRailContext = testRailContext;
        this.prFileNameDiffService = prFileNameDiffService;
        this.auditArtifactWriterService = auditArtifactWriterService;
        this.scenarioInfoProxyService = scenarioInfoProxyService;
        this.auditArtifactModelStorageService = auditArtifactModelStorageService;
    }

    /**
     * @implNote this method is usually the hand off for the post to the testrail client when sending run results for a scenario
     * there can be multiple scenarios ran in a single run, so the artifact should be a static hash of:
     *   HashSet<String, List<String>> or something to help capture tests / scenarios that are not tagged appropriately
     */
    @Override
    public void gaugeResult(Messages.ScenarioInfo scenarioInfo, String executionTime) {
        try {
            addUnannotatedTests(scenarioInfo);
        } catch (Exception e) {
            logger.warning(() -> "Unable to add unannotated tests" + e.getMessage() + ":\n " + e.getStackTrace());
        }

        try {
            saveUnannotatedTestArtifact();
        } catch (IOException e) {
            logger.warning(() -> "Unable to save unannotated tests" + e.getMessage() + ":\n " + e.getStackTrace());
        }
    }

    protected Set<String> buildModifiedSpecListForScenario(Messages.ScenarioInfo scenarioInfo) throws IOException {
        if (IS_FILE_PATH_FOR_SCENARIOS_TOTALLY_BOGUS) {
            return new HashSet<String>();
        }

        String file = scenarioInfoProxyService.getFileName(scenarioInfo);
        return this.prFileNameDiffService.providePRFileNameDiff().stream()
                                  .filter(f -> f.contains(file))
                                    .collect(Collectors.toSet());
    }

    protected synchronized void addUnannotatedTests(Messages.ScenarioInfo scenarioInfo) throws IOException {
        final Set<String> modifiedSpecSet = buildModifiedSpecListForScenario(scenarioInfo);

        List<String> unnanotatedTests = scenarioInfoProxyService.getTagsList(scenarioInfo).stream()
                                                    .filter(id -> !TestRailUtil.isTestRailTag(id))
                                                        .collect(Collectors.toList());
        if (unnanotatedTests.isEmpty() || (!IS_FILE_PATH_FOR_SCENARIOS_TOTALLY_BOGUS && modifiedSpecSet.isEmpty())) {
            // we didn't modify this spec this run, or the file path for scenarios is bogus :(
            return;
        }

        String scenarioName = scenarioInfoProxyService.getScenarioName(scenarioInfo);
        auditArtifactModelStorageService.storeTests(scenarioName, unnanotatedTests);
    }

    /**
     * @implNote this is kind of lazy, however as gauge tests can be multithreaded, we should synchronize this method
     *   to prevent any IO mishaps
     * @throws IOException
     */
    protected synchronized void saveUnannotatedTestArtifact() throws IOException {
        if (auditArtifactModelStorageService.any()) {
            auditArtifactWriterService.write(auditArtifactModelStorageService.getUnannotatedTestsByScenario(), UNANNOTATED_TEST_ARTIFACT_PATH);
        }
    }
}
