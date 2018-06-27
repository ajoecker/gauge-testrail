package de.nexible.gauge.testrail;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.context.GaugeContext;
import de.nexible.gauge.testrail.context.TestRailContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.nexible.gauge.testrail.SpecBuildHelper.suiteResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class GaugeLastRunTest {
    @Test
    public void persistedRunCanBeRecovered() {
        Spec.ProtoSuiteResult expected = suiteResult();
        try {
            Path lastrun = Files.createTempFile("lastrun", ".json");
            lastrun.toFile().deleteOnExit();
            GaugeContext gaugeContext = new GaugeContext() {
                @Override
                public String getGaugeProjectRoot() {
                    try {
                        return Files.createTempDirectory("lastrun").toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                @Override
                public String getGaugeReportsDir() {
                    return "reports";
                }
            };

            GaugeLastRun gaugeLastRun = new GaugeLastRun(gaugeContext, Mockito.mock(TestRailContext.class));
            gaugeLastRun.persistRun(expected);

            assertEquals(expected, gaugeLastRun.recoverLastRun());
        } catch (IOException e) {
            fail(e);
        }
    }

}
