package de.nexible.gauge.testrail.model;

import org.json.simple.JSONObject;

/**
 * A model class to hold the test results for a test case.
 *
 * The attributes of the objects are mapped to the api fields from TestRail
 *
 * @author ajoecker
 */
public final class TestResult {
    // format of variables match testrail api fields
    private String case_id;
    private String comment;
    private String elapsed;
    private int status_id;

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("case_id", case_id);
        jsonObject.put("comment", comment);
        jsonObject.put("elapsed", elapsed);
        jsonObject.put("status_id", status_id);
        return jsonObject;
    }

    public static final class TestResultBuilder {
        private final TestResult tr;

        private TestResultBuilder() {
            tr = new TestResult();
        }

        public static TestResultBuilder newTestResult() {
            TestResultBuilder testResultBuilder = new TestResultBuilder();
            return testResultBuilder;
        }

        public TestResultBuilder forCase(String caseId) {
            tr.case_id = caseId;
            return this;
        }

        public TestResultBuilder withStatus(int status) {
            tr.status_id = status;
            return this;
        }

        public TestResultBuilder elapsed(String timeFormat) {
            tr.elapsed = timeFormat;
            return this;
        }

        public TestResultBuilder comment(String comment) {
            tr.comment = comment;
            return this;
        }

        public TestResult done() {
            return tr;
        }
    }
}
