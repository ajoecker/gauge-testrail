package de.nexible.gauge.testrail;

import com.thoughtworks.gauge.Spec;

public interface GaugeResultListener {
    void gaugeResult(Spec.ProtoSuiteResult suiteResult);
}
