package de.nexible.gauge.testrail.services;

import java.io.IOException;
import java.util.List;

public interface PRSpecFileNameDiffService {
    /**
     * Provides a diff for the current pull request against master, returning only modified or added spec files
     * @return
     * @throws IOException
     */
    List<String> providePRFileNameDiff() throws IOException;
}
