package de.nexible.gauge.testrail.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.*;
import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentTest {
  @Test
  @DisplayName("Environment variables can be set with underscores as well as period-separated names")
  public void environmentVariablesCanBeSetWithUnderscoresAsWellAsPeriodSeparatedNames() throws Exception {
    List<String> actual = List.of(
        withEnvironmentVariable("TESTRAIL_URL", "https://underscores.testrail.io")
            .execute(() -> Environment.get("testrail.url")),
        withEnvironmentVariable("testrail.url", "https://period-separated.testrail.io")
            .execute(() -> Environment.get("testrail.url")));
    assertThat(actual).isEqualTo(List.of("https://underscores.testrail.io", "https://period-separated.testrail.io"));
  }

  @Test
  @DisplayName("Period separated names override names with underscores if both set")
  public void periodSeparatedOverridesUnderscoresIfBothSet() throws Exception {
    String actual = withEnvironmentVariable("TESTRAIL_URL", "https://underscores.testrail.io")
        .and("testrail.url", "https://period-separated.testrail.io").execute(() -> Environment.get("testrail.url"));
    assertThat(actual).isEqualTo("https://period-separated.testrail.io");
  }

}
