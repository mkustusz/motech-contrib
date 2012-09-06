package org.motechproject.paginator.repository;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.paginator.response.PageResults;
import org.motechproject.paginator.service.Paging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:test-applicationContext-Paginator.xml")
public class AllPagingServicesIT {

    @Autowired
    AllPagingServices allPagingServices;

    @Test
    public void shouldIncludeAllPagingServiceBeansInTheGeneralPagingServiceMap() {
        assertNotNull(allPagingServices.getPagingServiceFor("testEntity"));
        assertNull(allPagingServices.getPagingServiceFor("notAPagedEntity"));
    }
}

@Component
class PagingServiceStub implements Paging {


    @Override
    public PageResults<Object> page(Integer pageNo, Integer rowsPerPage, Properties criteria) {
        return null;
    }

    @Override
    public String entityName() {
        return "testEntity";
    }
}