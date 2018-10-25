package de.nexible.gauge.testrail.sync.sync;

import com.gurock.testrail.APIException;
import de.nexible.gauge.testrail.sync.context.TestRailSyncContext;
import de.nexible.gauge.testrail.sync.model.GaugeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.nexible.gauge.testrail.config.TestRailUtil.parseSectionId;
import static de.nexible.gauge.testrail.config.TestRailUtil.toSectionTag;

public class TestRailSectionSync implements Sync {
    private static final Logger logger = Logger.getLogger(TestRailSectionSync.class.getName());

    private TestRailSyncContext testRailContext;

    public TestRailSectionSync(TestRailSyncContext testRailContext) {
        this.testRailContext = testRailContext;
    }

    public List<GaugeSpec> sync(List<GaugeSpec> specData) {
        logger.info(() -> "Start sync sections");
        List<GaugeSpec> changedData = new ArrayList<>(specData.size());
        for (GaugeSpec s : specData) {
            if (s.hasTag()) {
                logger.info(() -> "'" + s.getHeading() + "' is already tagged (" + s.getTag() + "). Checking for updates");
                if (!testRailContext.isDryRun()) {
                    checkForSectionUpdate(s);
                }
            } else {
                try {
                    logger.info(() -> "'" + s.getHeading() + "' has no tag yet. Send to TestRail to get new");
                    s.setTag(getSectionTag(s));
                    logger.info(() -> "'" + s.getHeading() + "' has now received tag: " + s.getTag());
                } catch (IOException | APIException e) {
                    logger.log(Level.WARNING, e, () -> "Failed to sync section '" + s.getHeading() + "'");
                }
            }
            changedData.add(s);
        }
        return changedData;
    }

    private String getSectionTag(GaugeSpec s) throws IOException, APIException {
        if (testRailContext.isDryRun()) {
            logger.info(() -> "Running dry run - use artifical section tag");
            return "section_100";
        }
        return toSectionTag(createNewSection(s));
    }

    private long createNewSection(GaugeSpec s) throws IOException, APIException {
        int projectId = testRailContext.projectId();
        String heading = s.getHeading();

        Optional<Long> id = testRailContext.getTestRailClient().getSectionId(heading, projectId);
        if (id.isPresent()) {
            return id.get();
        }
        return testRailContext.getTestRailClient().addSection(projectId, heading);
    }

    private void checkForSectionUpdate(GaugeSpec s) {
        int sectionId = parseSectionId(s.getTag());
        try {
            String sectionName = testRailContext.getTestRailClient().getSection(sectionId);
            if (s.hasHeadingChanged(sectionName)) {
                testRailContext.getTestRailClient().updateSection(sectionId, s.getHeading());
            }
        } catch (IOException | APIException e) {
            logger.log(Level.WARNING, e, () -> "Failed to update section");
        }
    }
}
