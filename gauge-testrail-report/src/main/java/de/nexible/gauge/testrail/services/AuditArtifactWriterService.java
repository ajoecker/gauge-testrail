package de.nexible.gauge.testrail.services;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AuditArtifactWriterService {
    void write(Map<String, List<String>> unannotatedTestsByScenario, String filePath) throws IOError, IOException;
}
