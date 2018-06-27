package de.nexible.gauge.testrail.context;

import java.nio.file.Paths;

public class RerunGaugeContext extends GaugeContext {
    @Override
    public String getGaugeProjectRoot() {
        return Paths.get(System.getProperty("user.dir")).getParent().getParent().toString();
    }

    @Override
    public boolean isRerun() {
        return true;
    }

    @Override
    public String getGaugeReportsDir() {
        return "reports";
    }
}
