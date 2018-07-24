package de.nexible.gauge.testrail;

import com.thoughtworks.gauge.Messages;

public interface GaugeResultListener {
    void gaugeResult(Messages.ScenarioInfo scenarioInfo, String executionTime);
}
