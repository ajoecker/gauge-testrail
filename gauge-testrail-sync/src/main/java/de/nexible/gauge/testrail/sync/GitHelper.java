package de.nexible.gauge.testrail.sync;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

public class GitHelper {
    private final SynConfiguration synConfiguration;

    GitHelper(SynConfiguration synConfiguration) {
        this.synConfiguration = synConfiguration;
    }

    public void cloneRepo() throws GitAPIException {
        CredentialsProvider credentialsProvider
                = new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", synConfiguration.getApiToken());
        Git git = Git.cloneRepository()
                .setURI(synConfiguration.getProjectUri())
                .setDirectory(synConfiguration.getLocalDirectory())
                .setCredentialsProvider(credentialsProvider)
                .call();
        git.close();
    }
}
