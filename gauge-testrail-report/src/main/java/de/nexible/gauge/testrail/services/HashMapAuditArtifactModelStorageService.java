package de.nexible.gauge.testrail.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashMapAuditArtifactModelStorageService implements AuditArtifactModelStorageService {
    private final HashMap<String, List<String>> unannotatedTestsByScenario = new HashMap<>();

    @Override
    synchronized public void storeTests(String scenarioName, List<String> testTags) {
        if (unannotatedTestsByScenario.containsKey(scenarioName)) {
            unannotatedTestsByScenario.get(scenarioName).addAll(testTags);
        } else {
            unannotatedTestsByScenario.put(scenarioName, testTags);
        }
    }

    @Override
    synchronized public Map<String, List<String>> getUnannotatedTestsByScenario() {
        return unannotatedTestsByScenario;
    }

    @Override
    synchronized public boolean any() {
        return !unannotatedTestsByScenario.isEmpty();
    }
}
