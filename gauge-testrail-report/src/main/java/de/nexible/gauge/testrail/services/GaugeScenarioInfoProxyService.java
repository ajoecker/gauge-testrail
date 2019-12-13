package de.nexible.gauge.testrail.services;

import com.thoughtworks.gauge.Messages;

import java.util.List;

public class GaugeScenarioInfoProxyService implements ScenarioInfoProxyService {
    @Override
    public List<String> getTagsList(Messages.ScenarioInfo scenarioInfo) {
        return scenarioInfo.getTagsList();
    }

    @Override
    public String getFileName(Messages.ScenarioInfo scenarioInfo) {
        return scenarioInfo.getDescriptorForType().getFile().getName();
    }

    @Override
    public String getScenarioName(Messages.ScenarioInfo scenarioInfo) {
        return scenarioInfo.getName();
    }
}
