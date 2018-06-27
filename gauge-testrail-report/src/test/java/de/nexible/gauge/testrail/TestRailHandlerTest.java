package de.nexible.gauge.testrail;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Spec;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static de.nexible.gauge.testrail.model.TestResult.TestResultBuilder.newTestResult;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class TestRailHandlerTest {
    private TestContext testRailContext;

    @BeforeEach
    public void setup() {
        testRailContext = new TestContext();
        testRailContext.testrunId = "5";
    }

    @Test
    @DisplayName("Given no TestRail run id, no data is sent to TestRail")
    public void noTestRailRunIdGiven() throws IOException, APIException {
        testRailContext.testrunId = null;
        handle(scenario("C234"));
        verifyZeroInteractions(testRailContext.apiClient);
    }

    @Test
    @DisplayName("Given no test results, no data is sent to TestRail")
    public void noTestResultsGiven() throws IOException, APIException {
        handle(Spec.ProtoScenario.newBuilder().build());
        verifyZeroInteractions(testRailContext.apiClient);
    }

    @Test
    @DisplayName("Given test results without any tags, no data is sent to TestRail")
    public void noTagsGiven() throws IOException, APIException {
        handle(scenario(null));
        verifyZeroInteractions(testRailContext.apiClient);
    }

    @Test
    @DisplayName("Given test results with tags, but not TestRail tags, data is sent to TestRail")
    public void noTestRailTags() throws IOException, APIException {
        handle(scenario("smoke"));
        verifyZeroInteractions(testRailContext.apiClient);
    }

    @Test
    @DisplayName("Given test results with TestRail case id tags, data is sent to TestRail")
    public void testRailTags() throws IOException, APIException {
        handle(scenario("C234"));
        verify(testRailContext.apiClient).sendPost(eq("add_results_for_cases/5"), eq(buildExpected("C234")));
    }

    @Test
    @DisplayName("Given test results with multiple TestRail case id tags, data is sent to TestRail")
    public void multipleTestRailTags() throws IOException, APIException {
        handle(scenario("C234", "smoke", "C567"));
        verify(testRailContext.apiClient).sendPost(eq("add_results_for_cases/5"), eq(buildExpected("C234", "C567")));
    }

    private JSONObject buildExpected(String... tags) {
        JSONObject expected = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (String tag : tags) {
            jsonArray.add(newTestResult().comment("Test executed automatically uploaded by Gauge at " + TestRailHandler.UPLOAD_TIME + ". Testcase: 'a simple test'")
                    .elapsed("1.234s").withStatus(1).forCase(tag.substring(1)).done().toJsonObject());
        }
        expected.put("results", jsonArray);
        return expected;
    }

    private Spec.ProtoScenario scenario(String... tag) {
        Spec.ProtoScenario.Builder scenario = Spec.ProtoScenario.newBuilder();
        Spec.ProtoStep step = Spec.ProtoStep.newBuilder().setActualText("This is a simple step").build();
        Spec.ProtoItem scenarioItem = Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Step).setStep(step).build();
        scenario.setScenarioHeading("a simple test").addScenarioItems(scenarioItem).setExecutionStatusValue(1).setExecutionTime(1234);

        if (tag != null) {
            Arrays.stream(tag).forEach(scenario::addTags);
        }

        return scenario.build();
    }

    private void handle(Spec.ProtoScenario scenario) throws IOException, APIException {
        new TestRailHandler(testRailContext).handle(asList(scenario));
    }

    private static final class TestContext extends TestRailContext {
        private String testrunId;
        private APIClient apiClient = mock(APIClient.class);


        @Override
        public APIClient getTestRailClient() {
            return apiClient;
        }


        @Override
        public String getTestRailRunId() {
            return testrunId;
        }
    }
}
