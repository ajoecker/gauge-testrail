package de.nexible.gauge.testrail.sync;

import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.gauge.GaugeDaemon;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

public class TestRailSync {
    private final SynConfiguration synConfiguration;
    private final GitHelper gitHelper;
    private final GaugeDaemon gaugeDaemon;

    public static void main(String[] args) throws IOException, GitAPIException {
        SynConfiguration synConfiguration = new SynConfiguration();
        synConfiguration.initFrom("/sync.properties");
        try {
            GitHelper gitHelper = new GitHelper(synConfiguration);
            GaugeDaemon gaugeDaemon = new GaugeDaemon(synConfiguration);
            new TestRailSync(synConfiguration, gitHelper, gaugeDaemon).start();
        }
        finally {
            System.out.println("WORKING FOR NOW ON " + synConfiguration.getLocalDirectory());
            //synConfiguration.cleanUp();
        }
    }

    TestRailSync(SynConfiguration synConfiguration, GitHelper gitHelper, GaugeDaemon gaugeDaemon) {
        this.synConfiguration = synConfiguration;
        this.gitHelper = gitHelper;
        this.gaugeDaemon = gaugeDaemon;
    }

    private void start() throws GitAPIException, IOException {
        gitHelper.cloneRepo();
        List<Spec.ProtoSpec> protoSpecs = gaugeDaemon.readSpecifications();
    }
}
