package de.nexible.gauge.testrail.config;

import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class TestRailUtil {
    private static final String SECTION = "section_";
    private static final Pattern TESTCASE_PATTERN = Pattern.compile("^[Cc]\\d+$");
    private static final Pattern SECTION_PATTERN = Pattern.compile("^" + SECTION + "\\d+$");

    private TestRailUtil() {
        // static
    }

    public static boolean isTestRailTag(String s) {
        return TESTCASE_PATTERN.matcher(s).find();
    }

    public static boolean isSectionTag(String s) {
        return SECTION_PATTERN.matcher(s).find();
    }

    public static int parseCaseId(String caseTag) {
        return parseInt(caseTag.substring(1));
    }

    public static int parseSectionId(String sectionTag) {
        return parseInt(sectionTag.replace(SECTION, ""));
    }

    public static String toSectionTag(long newSection) {
        return SECTION + newSection;
    }
}
