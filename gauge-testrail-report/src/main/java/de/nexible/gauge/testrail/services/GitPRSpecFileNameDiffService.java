package de.nexible.gauge.testrail.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GitPRSpecFileNameDiffService implements PRSpecFileNameDiffService {
    @Override
    public List<String> providePRFileNameDiff() throws IOException {
        // from the parallel ruby code:
        //        changed_spec_files = `git diff --name-status master | grep "spec.rb"`.strip.split(/\n/)
        //          .select { |line| line[0] == 'M' || line[0] == 'A' }
        //          .map { |line| line.split(/\t/)[1] }

        List<String> list = new ArrayList<String>();

        Process diffProcess = Runtime.getRuntime().exec("git diff --name-status master");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        }

        // we just want the spec files that were modified or added, and only those that match the current spec
        return list.stream().filter(f -> f.contains(".spec") && (f.charAt(0) == 'M' || f.charAt(0) == 'A'))
                .map(f -> f.split("\t")[1])
                .collect(Collectors.toList());
    }
}
