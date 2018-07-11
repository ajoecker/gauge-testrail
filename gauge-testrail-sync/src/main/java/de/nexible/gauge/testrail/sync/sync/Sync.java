package de.nexible.gauge.testrail.sync.sync;

import de.nexible.gauge.testrail.sync.model.GaugeSpec;

import java.util.List;

public interface Sync {
    List<GaugeSpec> sync(List<GaugeSpec> specData);
}
