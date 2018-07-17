package de.nexible.gauge.testrail.sync.sync;

import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.nio.file.Files.readAllLines;

public class SpecModifier implements Sync {
    @Override
    public List<GaugeSpec> sync(List<GaugeSpec> mods) {
        for (GaugeSpec gaugeSpec : mods) {
            Path specFile = gaugeSpec.getSpecFile();
            try {
                List<String> lines = read(specFile);

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (line.startsWith("##")) {
                        Optional<GaugeScenario> t = gaugeSpec.findScenarioByName(line);
                        if (t.isPresent() && t.get().hasBeenTagged()) {
                            i = changeSpecFile(lines, i, t.get().getTag());
                        }
                    } else if (line.startsWith("#") && gaugeSpec.hasBeenTagged()) {
                        i = changeSpecFile(lines, i, gaugeSpec.getTag());
                    }
                }
                write(specFile, lines);
            } catch (IOException e) {
                // TODO logger
                e.printStackTrace();
            }
        }
        return mods;
    }

    private int changeSpecFile(List<String> lines, int i, String tag) {
        int n = skipEmptyLines(lines, i);
        if (lines.get(n).trim().startsWith("tags")) {
            lines.set(n, lines.get(n) + ", " + tag);
        } else {
            lines.add(i + 1, "tags: " + tag);
        }
        return n;
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
