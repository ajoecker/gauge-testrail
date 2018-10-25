package com.gurock.testrail;

import com.google.common.collect.ImmutableMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Optional;

public class TestRailClient {
    private APIClient client;

    public TestRailClient(APIClient client) {
        this.client = client;
    }

    public static TestRailClient newClient(String url, String user, String token) {
        APIClient client = new APIClient(url);
        client.setUser(user);
        client.setPassword(token);
        return new TestRailClient(client);
    }

    public void addResult(String testRailRunId, JSONObject jsonObject) throws IOException, APIException {
        client.sendPost("add_results_for_cases/" + testRailRunId, jsonObject);
    }

    public long addSection(int projectId, String heading) throws IOException, APIException {
        JSONObject result = (JSONObject) client.sendPost("add_section/" + projectId, ImmutableMap.of("name", heading));
        return (long) result.get("id");
    }

    public void updateSection(int sectionId, String specName) throws IOException, APIException {
        client.sendPost("update_section/" + sectionId, ImmutableMap.of("name", specName));
    }

    public String getSection(int sectionId) throws IOException, APIException {
        JSONObject get = (JSONObject) client.sendGet("get_section/" + sectionId);
        return String.valueOf(get.get("name"));
    }

    public String updateCase(int caseId, JSONObject data) throws IOException, APIException {
        JSONObject result = (JSONObject) client.sendPost("update_case/" + caseId, data);
        return "C" + result.get("id");
    }

    public String addCase(int sectionId, JSONObject data) throws IOException, APIException {
        JSONObject result = (JSONObject) client.sendPost("add_case/" + sectionId, data);
        return "C" + result.get("id");
    }

    public Optional<Long> getSectionId(String heading, int projectId) throws IOException, APIException {
        JSONArray result = (JSONArray) client.sendGet("get_sections/" + projectId);
        for(Object o : result) {
            JSONObject jsonObject = (JSONObject) o;
            String name = String.valueOf(jsonObject.get("name"));
            if (heading.equalsIgnoreCase(name)) {
               return Optional.of((long)jsonObject.get("id"));
            }
        }
        return Optional.empty();
    }
}
