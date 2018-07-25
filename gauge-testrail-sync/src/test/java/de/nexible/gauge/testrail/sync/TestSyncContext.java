package de.nexible.gauge.testrail.sync;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.TestRailClient;
import de.nexible.gauge.testrail.sync.context.TestRailSyncDefaultContext;
import org.mockito.Mockito;

final class TestSyncContext extends TestRailSyncDefaultContext {
    private APIClient client;

    public TestSyncContext(APIClient client) {
        this.client = client;
    }

    @Override
    public TestRailClient getTestRailClient() {
        return new TestRailClient(client);
    }

    @Override
    public int projectId() {
        return 1;
    }

    @Override
    public int getTemplateId() {
        return 5;
    }
}
