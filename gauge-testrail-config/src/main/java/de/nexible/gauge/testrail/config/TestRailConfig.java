package de.nexible.gauge.testrail.config;

import java.util.regex.Pattern;

public class TestRailConfig {
    private static final String SECTION = "section_";
    private static final Pattern TESTCASE_PATTERN = Pattern.compile("^[Cc]\\d+$");
    private static final Pattern SECTION_PATTERN = Pattern.compile("^" + SECTION + "\\d+$");

    private TestRailConfig() {
        // static
    }

    public static boolean isTestRailTag(String s) {
        return TESTCASE_PATTERN.matcher(s).find();
    }

    public static boolean isSectionTag(String s) {
        return SECTION_PATTERN.matcher(s).find();
    }

    public static int parseSectionId(String sectionTag) {
        return Integer.parseInt(sectionTag.replace(SECTION, ""));
    }

    public static String toSectionTag(int newSection) {
        return SECTION + newSection;
    }
}
