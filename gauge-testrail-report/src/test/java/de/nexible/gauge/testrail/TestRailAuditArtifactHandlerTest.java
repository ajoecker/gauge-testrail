package de.nexible.gauge.testrail;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.TestRailClient;
import com.thoughtworks.gauge.Messages;
import de.nexible.gauge.testrail.context.TestRailReportDefaultContext;
import de.nexible.gauge.testrail.services.*;
import org.assertj.core.util.Lists;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static de.nexible.gauge.testrail.model.TestResult.TestResultBuilder.newTestResult;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class TestRailAuditArtifactHandlerTest {
    private TestReportDefaultContext testRailContext;

    @BeforeEach
    public void setup() {
        testRailContext = new TestReportDefaultContext();
        testRailContext.testrunId = "5";
    }

    @Test
    @DisplayName("Given test results without TestRail tags, the artifact contains those tests")
    public void testResultsWithoutTestRailTags() throws IOException {

        Messages.ScenarioInfo c31337_scen = Messages.ScenarioInfo.newBuilder().addTags("").build();

        ScenarioInfoProxyService scenarioInfoProxyServiceMock = mock(ScenarioInfoProxyService.class);

        when(scenarioInfoProxyServiceMock.getFileName(any())).thenReturn("test.spec");
        when(scenarioInfoProxyServiceMock.getScenarioName(any())).thenReturn("test");
        when(scenarioInfoProxyServiceMock.getTagsList(any())).thenReturn(Lists.list(""));

        PRSpecFileNameDiffService specDiffServiceMock = mock(PRSpecFileNameDiffService.class);

        when(specDiffServiceMock.providePRFileNameDiff()).thenReturn(Lists.list("test/some/package/test.spec"));

        TestRailAuditArtifactHandler c31337 = handler(specDiffServiceMock, new AuditArtifactWriterService() {
            @Override
            public void write(Map<String, List<String>> unannotatedTestsByScenario, String filePath) throws IOError, IOException {
                Assert.assertTrue(unannotatedTestsByScenario.size() == 1);
                Assert.assertTrue(unannotatedTestsByScenario.get("test").size() == 1);
            }
        }, scenarioInfoProxyServiceMock, new HashMapAuditArtifactModelStorageService());

        c31337.gaugeResult(c31337_scen, "sometime");

        verify(c31337).saveUnannotatedTestArtifact();
        verify(c31337).buildModifiedSpecListForScenario(any(Messages.ScenarioInfo.class));
        verify(c31337).addUnannotatedTests(any(Messages.ScenarioInfo.class));

        verifyZeroInteractions(testRailContext.apiClient);
    }

    @Test
    @DisplayName("Given test results with tags but not for testrail, we expect a audit artifact")
    public void testResultsWithTaggedNonTestRailIds() throws IOException {

        Messages.ScenarioInfo c31337_scen = Messages.ScenarioInfo.newBuilder().addTags("T3000").build();

        ScenarioInfoProxyService scenarioInfoProxyServiceMock = mock(ScenarioInfoProxyService.class);

        when(scenarioInfoProxyServiceMock.getFileName(any())).thenReturn("test.spec");
        when(scenarioInfoProxyServiceMock.getScenarioName(any())).thenReturn("test");
        when(scenarioInfoProxyServiceMock.getTagsList(any())).thenReturn(Lists.list("T3000"));

        PRSpecFileNameDiffService specDiffServiceMock = mock(PRSpecFileNameDiffService.class);

        when(specDiffServiceMock.providePRFileNameDiff()).thenReturn(Lists.list("test/some/package/test.spec"));

        TestRailAuditArtifactHandler c31337 = handler(specDiffServiceMock, new AuditArtifactWriterService() {
            @Override
            public void write(Map<String, List<String>> unannotatedTestsByScenario, String filePath) throws IOError, IOException {
                Assert.assertTrue(unannotatedTestsByScenario.size() == 1);
                Assert.assertTrue(unannotatedTestsByScenario.get("test").size() == 1);
                Assert.assertTrue(unannotatedTestsByScenario.get("test").get(0) == "T3000");
            }
        }, scenarioInfoProxyServiceMock, new HashMapAuditArtifactModelStorageService());

        c31337.gaugeResult(c31337_scen, "sometime");

        verify(c31337).saveUnannotatedTestArtifact();
        verify(c31337).buildModifiedSpecListForScenario(any(Messages.ScenarioInfo.class));
        verify(c31337).addUnannotatedTests(any(Messages.ScenarioInfo.class));

        verifyZeroInteractions(testRailContext.apiClient);
    }


    @Test
    @DisplayName("Given test results with testrail tags, we expect no artifact")
    public void testResultsHaveTestRailTags() throws IOException {
        Messages.ScenarioInfo c31337_scen = Messages.ScenarioInfo.newBuilder().addTags("C31337").build();

        ScenarioInfoProxyService scenarioInfoProxyServiceMock = mock(ScenarioInfoProxyService.class);

        when(scenarioInfoProxyServiceMock.getFileName(any())).thenReturn("test.spec");
        when(scenarioInfoProxyServiceMock.getScenarioName(any())).thenReturn("test");
        when(scenarioInfoProxyServiceMock.getTagsList(any())).thenReturn(Lists.list("C31337"));

        AuditArtifactWriterService auditArtifactWriterService = mock(AuditArtifactWriterService.class);

        PRSpecFileNameDiffService specDiffServiceMock = mock(PRSpecFileNameDiffService.class);

        when(specDiffServiceMock.providePRFileNameDiff()).thenReturn(Lists.list("test/some/package/test.spec"));

        TestRailAuditArtifactHandler c31337 = handler(
                specDiffServiceMock,
                auditArtifactWriterService,
                scenarioInfoProxyServiceMock,
                new HashMapAuditArtifactModelStorageService());

        c31337.gaugeResult(c31337_scen, "sometime");

        verify(c31337).saveUnannotatedTestArtifact();
        verify(c31337).buildModifiedSpecListForScenario(any(Messages.ScenarioInfo.class));
        verify(c31337).addUnannotatedTests(any(Messages.ScenarioInfo.class));

        verifyZeroInteractions(testRailContext.apiClient);
        verifyZeroInteractions(auditArtifactWriterService);
    }

    private TestRailAuditArtifactHandler handler(
            PRSpecFileNameDiffService specFileNameDiffService,
            AuditArtifactWriterService writerService,
            ScenarioInfoProxyService scenarioProxyService,
            AuditArtifactModelStorageService auditArtifactModelStorageService) {
        return Mockito.spy(new TestRailAuditArtifactHandler(
                testRailContext,
                specFileNameDiffService,
                writerService,
                scenarioProxyService,
                auditArtifactModelStorageService));
    }

    private static final class TestReportDefaultContext extends TestRailReportDefaultContext {
        private String testrunId;
        private APIClient apiClient = mock(APIClient.class);

        @Override
        public String getTestRailRunId() {
            return testrunId;
        }

        public TestRailClient getTestRailClient() {
            return new TestRailClient(apiClient);
        }
    }
}
