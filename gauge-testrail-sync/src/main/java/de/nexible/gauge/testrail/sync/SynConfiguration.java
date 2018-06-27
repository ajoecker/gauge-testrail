package de.nexible.gauge.testrail.sync;

import de.nexible.gauge.testrail.sync.gauge.GaugeDaemon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;

import static java.lang.Integer.parseInt;
import static java.nio.file.Files.walk;

public class SynConfiguration {
    private Properties properties;
    private Path tempDirectory;

    public SynConfiguration() {
        this.properties = new Properties();
    }

    public String getApiToken() {
        return properties.getProperty("nexible.end2end.token");
    }

    public File getLocalDirectory() {
        return tempDirectory.toFile();
    }

    public String getProjectUri() {
        return properties.getProperty("nexible.end2end.project");
    }

    public void initFrom(String propertiesURI) throws IOException {
        try (InputStream ins = GaugeDaemon.class.getResourceAsStream(propertiesURI)) {
            properties.load(ins);
        }

        tempDirectory = Files.createTempDirectory("gauge-com.gurock.testrail-sync");
        //tempDirectory = Paths.get("C:\\Users\\ajoecker\\AppData\\Local\\Temp\\2\\gauge-com.gurock.testrail-sync5269092460656454104");
    }

    public void cleanUp() throws IOException {
        walk(tempDirectory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public int getDaemonPort() {
        return parseInt(properties.getProperty("nexible.end2end.daemon.port"));
    }

    public Path getGaugeWorkingDirectory() {
        return tempDirectory.resolve(properties.getProperty("nexible.end2end.backend"));
    }

    public String getDaemonServer() {
        return properties.getProperty("nexible.end2end.daemon.server");
    }
}
