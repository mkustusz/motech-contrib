package org.motechproject.event.aggregation.model;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.motechproject.event.aggregation.model.mapper.AggregationScheduleMapper;
import org.motechproject.event.aggregation.model.schedule.*;

import static junit.framework.Assert.assertEquals;
import static org.motechproject.commons.date.util.DateUtil.newDateTime;
import static org.motechproject.commons.date.util.DateUtil.setTimeZoneUTC;

public class AggregationScheduleMapperTest {

    AggregationScheduleMapper mapper;

    @Before
    public void setup() {
        mapper = new AggregationScheduleMapper();
    }

    @Test
    public void shouldMapPeriodicScheduleRequest() {
        DateTime startTime = newDateTime(2012, 5, 22);
        PeriodicAggregationRecord record = (PeriodicAggregationRecord) mapper.toRecord(new PeriodicAggregationRequest("1 day", startTime));
        assertEquals(Period.days(1), record.getPeriod());
        assertEquals(setTimeZoneUTC(startTime), record.getStartTime());
    }

    @Test
    public void shouldMapCronScheduleRequest() {
        CronBasedAggregationRecord record = (CronBasedAggregationRecord) mapper.toRecord(new CronBasedAggregationRequest("* * * * *"));
        assertEquals("* * * * *", record.getCronExpression());
    }

    @Test
    public void shouldMapCustomScheduleRequest() {
        CustomAggregationRecord record = (CustomAggregationRecord) mapper.toRecord(new CustomAggregationRequest("true"));
        assertEquals("true", record.getRule());
    }

    @Test
    public void shouldMapPeriodicScheduleRecord() {
        PeriodicAggregationRequest request = (PeriodicAggregationRequest) mapper.toRequest(new PeriodicAggregationRecord(Period.days(1), newDateTime(2012, 5, 22)));
        assertEquals(Period.days(1), request.getPeriod());
        assertEquals(newDateTime(2012, 5, 22), request.getStartTime());
    }

    @Test
    public void shouldMapCronScheduleRecord() {
        CronBasedAggregationRequest request = (CronBasedAggregationRequest) mapper.toRequest(new CronBasedAggregationRecord("* * * * *"));
        assertEquals("* * * * *", request.getCronExpression());
    }

    @Test
    public void shouldMapCustomScheduleRecord() {
        CustomAggregationRequest request = (CustomAggregationRequest) mapper.toRequest(new CustomAggregationRecord("true"));
        assertEquals("true", request.getRule());
    }
}
