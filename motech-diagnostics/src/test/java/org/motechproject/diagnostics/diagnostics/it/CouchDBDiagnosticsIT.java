package org.motechproject.diagnostics.diagnostics.it;

import org.ektorp.CouchDbConnector;
import org.ektorp.impl.StdCouchDbConnector;
import org.junit.Test;
import org.motechproject.diagnostics.diagnostics.CouchDBDiagnostics;
import org.motechproject.diagnostics.response.DiagnosticsResult;
import org.motechproject.testing.utils.SpringIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@ContextConfiguration(locations = "classpath:applicationContext-DiagnosticsTest.xml")
@DirtiesContext
public class CouchDBDiagnosticsIT extends SpringIntegrationTest {


    @Autowired
    private List<StdCouchDbConnector> allConnectors;

    @Autowired
    private CouchDBDiagnostics diagnostics;

    @Test
    public void eachInstanceHasResult() {
        List<DiagnosticsResult> results = diagnostics.isActive().getResults();
        assertEquals(allConnectors.size(), results.size());
    }

    @Test
    public void shouldReturnPositiveResultWhenAbleToConnectToInstance() {
        List<DiagnosticsResult> results = diagnostics.isActive().getResults();
        assertEquals("true", results.get(0).getValue().toString());
    }

    @Test
    public void shouldReturnNegativeReturnWhenNotAbleToConnectToInstance() {
        List<DiagnosticsResult> results = diagnostics.isActive().getResults();
        assertResultsContainError(results);
    }

    private void assertResultsContainError(List<DiagnosticsResult> results) {
        for (DiagnosticsResult result : results) {
            if ("error".equals(result.getValue().toString()))
                return;
        }
        fail("Expected a result with an error. But none found");
    }

    @Override
    public CouchDbConnector getDBConnector() {
        return null;
    }
}
