package org.motechproject.paginator.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.paginator.repository.AllPagingServices;
import org.motechproject.paginator.response.PageResults;
import org.motechproject.paginator.service.Paging;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.server.setup.MockMvcBuilders.standaloneSetup;

public class PaginationControllerTest {

    @Mock
    AllPagingServices allPagingServices;
    PaginationController paginationController;

    @Before
    public void setup() {
        initMocks(this);
        paginationController = new PaginationController(allPagingServices);
    }

    @Test
    public void shouldPageEntity() throws Exception {
        PageResults<String> results = new PageResults<>();
        results.setResults(asList("someString"));
        results.setPageNo(0);
        results.setTotalRows(1);

        Paging pagingService = mock(Paging.class);
        when(allPagingServices.getPagingServiceFor("entity1")).thenReturn(pagingService);
        when(pagingService.page(any(Integer.class), any(Integer.class))).thenReturn(results);

        standaloneSetup(paginationController).build()
                .perform(get("/page/entity1").param("pageNo", "1"))
                .andExpect(status().isOk())
                .andExpect(content().type("application/json;charset=UTF-8"))
                .andExpect(content().string("{\"pageNo\":0,\"totalRows\":1,\"results\":[\"someString\"]}"));
        verify(pagingService).page(eq(1), anyInt());
    }
}
