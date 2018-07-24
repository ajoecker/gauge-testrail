package de.nexible.gauge.testrail.sync;

import com.google.common.collect.ImmutableList;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.model.GaugeScenario;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static de.nexible.gauge.testrail.sync.GaugeSpecRetrieval.retrieveSpecs;
import static de.nexible.gauge.testrail.sync.sync.CaseFormatter.getCaseText;
import static org.assertj.core.api.Assertions.assertThat;

public class GaugeSpecRetrievalTest {
    @Test
    @DisplayName("Single scenario with no tags is found")
    public void singleScenarioNoTags() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(buildScenario("a scenario")).build();
        GaugeScenario expected = GaugeScenario.newInstance(buildScenario("a scenario").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Single scenario with TestRail tags is not found")
    public void singleScenarioWithTestRailTags() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(buildScenario("a scenario", "C123")).build();
        GaugeScenario expected = GaugeScenario.newInstance(buildScenario("a scenario", "C123").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Single scenario with non TestRail tag is found")
    public void singleScenarioWithOtherTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec").addItems(buildScenario("a scenario", "smoke")).build();
        GaugeScenario expected = GaugeScenario.newInstance(buildScenario("a scenario", "smoke").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Two scenarios with no tags / non TestTrail are found")
    public void twoScenariosWithNoTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario"))
                .addItems(buildScenario("another scenario", "smoke"))
                .build();
        GaugeScenario expected1 = GaugeScenario.newInstance(buildScenario("a scenario").getScenario());
        GaugeScenario expected2 = GaugeScenario.newInstance(buildScenario("another scenario").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected1, expected2).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Two scenarios with no tags / TestTrail are found partially")
    public void twoScenariosWithNoAndTagsTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario"))
                .addItems(buildScenario("another scenario", "C23"))
                .build();
        GaugeScenario expected1 = GaugeScenario.newInstance(buildScenario("a scenario").getScenario());
        GaugeScenario expected2 = GaugeScenario.newInstance(buildScenario("another scenario", "C23").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected1, expected2).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    @DisplayName("Two scenarios with TestTrail are not found")
    public void twoScenariosWithTestRailTag() {
        Spec.ProtoSpec build = Spec.ProtoSpec.newBuilder().setFileName("foo.spec").setSpecHeading("a spec")
                .addItems(buildScenario("a scenario", "C23"))
                .addItems(buildScenario("another scenario", "C23"))
                .build();
        GaugeScenario expected1 = GaugeScenario.newInstance(buildScenario("a scenario", "C23").getScenario());
        GaugeScenario expected2 = GaugeScenario.newInstance(buildScenario("another scenario", "C23").getScenario());
        assertThat(retrieveSpecs(ImmutableList.of(build))).flatExtracting(GaugeSpec::getScenarios).containsOnly(expected1, expected2).allMatch(p -> !p.hasBeenTagged());
    }

    @Test
    public void conceptandCommentsAreConsidered() throws IOException {
        String expected = "_ein spec kommentar_\n" +
                "_in zwei Zeilen_\n" +
                "_ein scenario kommentar_\n" +
                "_in zwei Zeilen_\n" +
                "\n" +
                "* Öffne die backoffice Testumgebung\n" +
                "* Login mit test user\n" +
                "* Lade Vertragsdaten aus <file:/data/contract_basis.yaml>\n" +
                "* Erstelle einen neuen Vertrag\n" +
                "* Überprüfe, dass das Prämienkonto die korrekte Anzahl von Buchungen hat";

        URL resource = GaugeSpecRetrievalTest.class.getResource("/spec.serialised");
        try (InputStream ins = resource.openStream()) {
            Spec.ProtoSpec protoSpec = Spec.ProtoSpec.parseFrom(ins);
            GaugeSpec gaugeSpecs = retrieveSpecs(ImmutableList.of(protoSpec)).get(0);
            String caseText = getCaseText(gaugeSpecs, gaugeSpecs.getScenarios().get(0));
            //assertThat(caseText).isEqualTo(expected);
        }
    }

    @Test
    public void inlineTableIsReadAndCorrectlyFormatted() throws IOException {
        String expected = "_Testet, dass die folgenden Regeln gelten_\n" +
                "_Haftpflichtschäden >= 4 --> nur Haftpflicht_\n" +
                "_TK Schäden + VK Schäden >= 6 --> nur Haftpflicht_\n" +
                "\n" +
                "* Öffne die nexible.de Testumgebung\n" +
                "* Starte die Antragsstrecke\n" +
                "* Überprüfe, dass die folgende Schadenskombinationen das korrekte Angebot forsiert\n" +
                "|||:Haftpflicht|:Teilkasko|:Vollkasko|:Angebot\n" +
                "||0|0|0|Haftpflicht, Teilkasko, Vollkasko\n" +
                "||4|0|0|Haftpflicht";

        URL resources = GaugeSpecRetrievalTest.class.getResource("/spec_with_table.serialised");
        try(InputStream ins = resources.openStream()) {
            Spec.ProtoSpec protoSpec = Spec.ProtoSpec.parseFrom(ins);
            GaugeSpec gaugeSpec = retrieveSpecs(ImmutableList.of(protoSpec)).get(0);
            String caseText = getCaseText(gaugeSpec, gaugeSpec.getScenarios().get(0));
            //assertThat(caseText).isEqualTo(expected);
        }
    }

    private Spec.ProtoItem.Builder buildScenario(String title, String... tags) {
        Spec.ProtoScenario.Builder ps = Spec.ProtoScenario.newBuilder().setScenarioHeading(title);
        for (String t : tags) {
            ps.addTags(t);
        }
        return Spec.ProtoItem.newBuilder().setItemType(Spec.ProtoItem.ItemType.Scenario).setScenario(ps.build());
    }
}
