package de.nexible.gauge.testrail.services;

import com.thoughtworks.gauge.Messages;

import java.util.List;

public interface ScenarioInfoProxyService {
    List<String> getTagsList(Messages.ScenarioInfo scenarioInfo);
    String getFileName(Messages.ScenarioInfo scenarioInfo);
    String getScenarioName(Messages.ScenarioInfo scenarioInfo);
}
