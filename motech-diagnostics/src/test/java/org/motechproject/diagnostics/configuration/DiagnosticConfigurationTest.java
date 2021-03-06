package org.motechproject.diagnostics.configuration;


import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

public class DiagnosticConfigurationTest{

    @Test
    public void shouldGetLinksFromProperties() {
        Properties diagnosticProperties = new Properties();
        diagnosticProperties.put("link.Menu1.Name1", "url1");
        diagnosticProperties.put("link.Menu1.Name2", "url2");
        diagnosticProperties.put("link.Menu2.Name3", "url3");
        DiagnosticConfiguration diagnosticConfiguration = new DiagnosticConfiguration(diagnosticProperties);

        List<Link> links = diagnosticConfiguration.getLinks();

        assertEquals("Menu1", links.get(0).getName());
        assertEquals("Menu2", links.get(1).getName());
        assertEquals(2, links.size());


        List<Link> firstMenuLinks = links.get(0).getLinks();
        assertThat(firstMenuLinks, hasItem(new Link("Name1", "url1")));
        assertThat(firstMenuLinks, hasItem(new Link("Name2", "url2")));
        assertThat(links.get(1).getLinks(), hasItem(new Link("Name3", "url3")));
    }

    @Test
    public void shouldReturnEmptyListIfNoScheduleJobNamesHaveBeenDefined() {
        DiagnosticConfiguration diagnosticConfiguration = new DiagnosticConfiguration(new Properties());
        assertTrue(diagnosticConfiguration.scheduleJobNames().isEmpty());
    }

    @Test
    public void shouldReturnListOfScheduleJobNames() {
        Properties diagnosticProperties = new Properties();
        String commaSeperatedJobNames = "job1,job2";
        diagnosticProperties.put("motech.scheduler.jobs", commaSeperatedJobNames);
        DiagnosticConfiguration diagnosticConfiguration = new DiagnosticConfiguration(diagnosticProperties);

        assertEquals(asList("job1", "job2"), diagnosticConfiguration.scheduleJobNames());
    }

    @Test
    public void shouldReturnQuartzDataSource() {
        Properties diagnosticProperties = new Properties();
        DiagnosticConfiguration diagnosticConfiguration = new DiagnosticConfiguration(diagnosticProperties);

        assertNull(diagnosticConfiguration.getQuartzDataSourceName());

        diagnosticProperties.put("quartz.dataSource", "motechDS");

        assertEquals("motechDS", diagnosticConfiguration.getQuartzDataSourceName());
    }

}
