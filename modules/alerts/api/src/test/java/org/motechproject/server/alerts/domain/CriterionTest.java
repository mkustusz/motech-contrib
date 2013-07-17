package org.motechproject.server.alerts.domain;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.server.alerts.contract.AlertCriteria;
import org.motechproject.server.alerts.repository.AllAlerts;

import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.on;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.commons.date.util.DateUtil.setTimeZoneUTC;

public class CriterionTest {
    @Mock
    private AllAlerts allAlerts;
    private AlertCriteria alertCriteria;

    @Before
    public void setUp() {
        initMocks(this);
        alertCriteria = new AlertCriteria();
    }

    @Test
    public void fetchAlerts_GivenADateRangeCriteria() {
        DateTime fromDate = DateUtil.newDateTime(new LocalDate(2010, 10, 10), 0, 0, 0);
        DateTime toDate = DateUtil.newDateTime(new LocalDate(2010, 10, 20), 0, 0, 0);
        alertCriteria.byDateRange(fromDate, toDate);

        ArrayList<Alert> expectedAlerts = new ArrayList<Alert>() {{
            add(new Alert());
        }};
        when(allAlerts.findByDateTime(fromDate, toDate)).thenReturn(expectedAlerts);

        List<Alert> actualAlerts = Criterion.dateRange.fetch(allAlerts, alertCriteria);
        assertEquals(expectedAlerts, actualAlerts);
    }

    @Test
    public void filterAlerts_GivenADateRangeCriteria() {
        final DateTime dayOne = DateUtil.newDateTime(new LocalDate(2010, 10, 1), 0, 0, 0);
        final DateTime dayTwo = DateUtil.newDateTime(new LocalDate(2010, 10, 2), 0, 0, 0);
        final DateTime dayThree = DateUtil.newDateTime(new LocalDate(2010, 10, 3), 0, 0, 0);

        alertCriteria.byDateRange(dayTwo, dayThree);

        ArrayList<Alert> allAlerts = new ArrayList<Alert>() {{
            add(new Alert(){{ setDateTime(dayOne); }});
            add(new Alert(){{ setDateTime(dayTwo); }});
            add(new Alert(){{ setDateTime(dayThree); }});
        }};

        List<Alert> filteredAlerts = Criterion.dateRange.filter(allAlerts, alertCriteria);

        assertEquals(asList(setTimeZoneUTC(dayTwo), setTimeZoneUTC(dayThree)), extract(filteredAlerts, on(Alert.class).getDateTime()));
    }
}
