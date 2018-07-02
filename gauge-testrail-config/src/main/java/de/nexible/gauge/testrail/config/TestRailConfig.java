package de.nexible.gauge.testrail.config;

import java.util.regex.Pattern;

public class TestRailConfig {
    private static final Pattern TESTRAIL_PATTERN = Pattern.compile("^[Cc]\\d+$");

    private TestRailConfig() {
        // static
    }

    public static boolean isTestRailTag(String s) {
        return TESTRAIL_PATTERN.matcher(s).find();
    }
}
