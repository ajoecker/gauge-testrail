package de.nexible.gauge.testrail.services;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonAuditArtifactWriterService implements AuditArtifactWriterService {
    @Override
    public void write(Map<String, List<String>> unannotatedTestsByScenario, String filePath) throws IOError, IOException {
        JSONObject json = new JSONObject();
        JSONObject testsByScenario = new JSONObject();
        for (Map.Entry<String, List<String>> entry : unannotatedTestsByScenario.entrySet()) {
            testsByScenario.put(entry.getKey(), entry.getValue().stream().collect(JSONArray::new, JSONArray::add, (x, y) -> {}));
        }

        json.put("unannotated-tests", testsByScenario);
        try (FileWriter writer = new FileWriter(filePath)) {
            json.writeJSONString(writer);
        }
    }
}
