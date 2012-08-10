package org.motechproject.diagnostics.diagnostics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.diagnostics.response.DiagnosticsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.JMSException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-Diagnostics.xml")
public class ActiveMQDiagnosticIT {

    @Autowired
    private ActiveMQDiagnostic activeMQDiagnostic;

    @Test
    public void shouldCheckActiveMQConnection() throws JMSException {
        DiagnosticsResult diagnosticsResult = activeMQDiagnostic.performDiagnosis();
        assertNotNull(diagnosticsResult);
        assertTrue("Connection check should return true.", diagnosticsResult.getStatus());
    }
}