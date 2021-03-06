package org.motechproject.caselogs.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.caselogs.configuration.CaseLogConfiguration;
import org.motechproject.caselogs.velocity.builder.CaseLogsResponseBuilder;
import org.motechproject.casexml.domain.CaseLog;
import org.motechproject.casexml.service.CaseLogService;

import java.util.ArrayList;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.setup.MockMvcBuilders.standaloneSetup;

public class CaseLogsControllerTest {

    public static final int NUMBER_OF_CASE_LOGS = 2;

    private CaseLogsController caseLogsController;

    @Mock
    private CaseLogService caseLogService;

    @Mock
    private CaseLogConfiguration configuration;

    @Mock
    private CaseLogsResponseBuilder caseLogsResponseBuilder;

    @Before
    public void setUp() {
        initMocks(this);
        when(configuration.getCaseLogsLimit()).thenReturn(NUMBER_OF_CASE_LOGS);
        caseLogsController = new CaseLogsController(caseLogService, caseLogsResponseBuilder, configuration);
    }

    @Test
    public void shouldGetAllCaseLogs() throws Exception {
        ArrayList<CaseLog> caseLogs = new ArrayList<>();
        when(caseLogService.getLatestLogs(NUMBER_OF_CASE_LOGS)).thenReturn(caseLogs);
        String expectedLogs = "our own case logs";
        when(caseLogsResponseBuilder.createResponseMessage(caseLogs, CaseLogsController.VIEW_PATH)).thenReturn(expectedLogs);

        standaloneSetup(caseLogsController)
                .build()
                .perform(get("/caselogs/all")
                ).andExpect(
                content().string(containsString(expectedLogs))
        );

        verify(caseLogService).getLatestLogs(NUMBER_OF_CASE_LOGS);
        verify(caseLogsResponseBuilder).createResponseMessage(caseLogs, CaseLogsController.VIEW_PATH);
    }

    @Test
    public void shouldFilterCaseLogs_byEntityIdAndOrRequestType() throws Exception {
        ArrayList<CaseLog> caseLogs = new ArrayList<>();
        String entityId = "entityId";
        String requestType = "Container Registration";
        when(caseLogService.filter(entityId, requestType, NUMBER_OF_CASE_LOGS)).thenReturn(caseLogs);
        String expectedLogs = "our own case logs";
        when(caseLogsResponseBuilder.createResponseMessage(caseLogs, CaseLogsController.CONTENT_PATH)).thenReturn(expectedLogs);

        standaloneSetup(caseLogsController)
                .build()
                .perform(post("/caselogs/filter").param("entityId", entityId).param("requestType", requestType)
                ).andExpect(
                content().string(containsString(expectedLogs))
        );

        verify(caseLogService).filter(entityId, requestType, NUMBER_OF_CASE_LOGS);
        verify(caseLogsResponseBuilder).createResponseMessage(caseLogs, CaseLogsController.CONTENT_PATH);
    }
}
