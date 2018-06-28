package de.nexible.gauge.testrail;

import org.junit.jupiter.api.Test;

import static de.nexible.gauge.testrail.TestRailTimespanHandler.toTimeFormat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRailTimespanHandlerTest {
    @Test
    public void underASecond() {
        assertEquals(toTimeFormat(100), "0.100s");
    }

    @Test
    public void oneSecond() {
        assertEquals(toTimeFormat(1000), "1.000s");
    }

    @Test
    public void withMinutes() {
        assertEquals(toTimeFormat(10860), "10.860s");
    }
}
