package de.nexible.gauge.testrail;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converts gauge execution time in milliseconds to TestRails time span format like <code>1m 20.234s</code>
 *
 * @author ajoecker
 */
public class TestRailTimespanHandler {
    private static final DateTimeFormatter WITH_MINUTES = DateTimeFormatter.ofPattern("m'm' s.SSS's'");
    private static final DateTimeFormatter WITHOUT_MINUTES = DateTimeFormatter.ofPattern("s.SSS's'");

    private TestRailTimespanHandler() {
        // static
    }

    public static String toTimeFormat(long time) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        if (zdt.getMinute() == 0) {
            return WITHOUT_MINUTES.format(zdt);
        }
        return WITH_MINUTES.format(zdt);
    }
}
