package de.nexible.gauge.testrail;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.context.GaugeDefaultReportContext;
import de.nexible.gauge.testrail.context.TestRailReportDefaultContext;
import org.junit.jupiter.api.Test;
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
            GaugeDefaultReportContext gaugeDefaultContext = new GaugeDefaultReportContext() {
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

            GaugeLastRun gaugeLastRun = new GaugeLastRun(gaugeDefaultContext, Mockito.mock(TestRailReportDefaultContext.class));
            gaugeLastRun.persistRun(expected);

            assertEquals(expected, gaugeLastRun.recoverLastRun());
        } catch (IOException e) {
            fail(e);
        }
    }

}
