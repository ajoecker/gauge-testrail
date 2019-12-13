package de.nexible.gauge.testrail.services;

import java.util.List;
import java.util.Map;

public interface AuditArtifactModelStorageService {
    void storeTests(String name, List<String> testTags);
    Map<String, List<String>> getUnannotatedTestsByScenario();
    boolean any();
}
