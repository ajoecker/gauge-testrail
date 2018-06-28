package de.nexible.gauge.testrail;

import com.gurock.testrail.APIException;
import com.thoughtworks.gauge.Spec;

import java.io.IOException;

public interface TestRailHandler {
    void handle(Spec.ProtoSuiteResult suiteResult) throws IOException, APIException;
}
