package de.nexible.gauge.testrail.sync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.nio.file.Files.readAllLines;

public class SpecModifier {
    final void persistChanges(List<SpecModifications> mods) throws IOException {
        for (SpecModifications specModifications : mods) {
            Path specFile = specModifications.getSpecFile();
            List<String> lines = read(specFile);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                Optional<String> relevantScenario = specModifications.isRelevantScenario(line);
                if (relevantScenario.isPresent()) {
                    String scenarioHeading = relevantScenario.get();
                    int n = skipEmptyLines(lines, i);
                    if (lines.get(n).trim().startsWith("tags")) {
                        lines.set(n, lines.get(n) + ", " + specModifications.getTag(scenarioHeading));
                    } else {
                        lines.add(i + 1, "tags: " + specModifications.getTag(scenarioHeading));
                    }
                }
            }
            write(specFile, lines);
        }
    }

    private List<String> read(Path specFile) throws IOException {
        return readAllLines(specFile);
    }

    protected void write(Path path, List<String> lines) throws IOException {
        Files.write(path, lines);
    }

    private int skipEmptyLines(List<String> lines, int i) {
        int n = i + 1;
        while (lines.get(n).trim().isEmpty()) {
            n++;
        }
        return n;
    }
}
