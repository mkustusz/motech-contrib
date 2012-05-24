package org.motechproject.retry.dao;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.motechproject.retry.domain.Retry;
import org.motechproject.retry.domain.RetryRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-retry.xml")
public class AllRetriesTest {
    @Autowired
    private AllRetries allRetries;

    @Test
    public void shouldLoadJsonForASimpleRetrySchedule() {
        String retryScheduleName = "Simple-Retry";

        RetryRecord retryRecord = allRetries.getRetryRecord(retryScheduleName);

        assertThat(retryRecord.name(), is(retryScheduleName));
        assertThat(retryRecord.retryCount(), is(5));
        assertThat(retryRecord.retryInterval(), is(Period.minutes(30)));
    }

    @Test
    public void shouldLoadJsonWithMultiplePeriods() {
        String retryScheduleName = "retry-every-2hrs-and-30mins";
        RetryRecord retryRecord = allRetries.getRetryRecord(retryScheduleName);

        assertThat(retryRecord.name(), is(retryScheduleName));
        assertThat(retryRecord.retryCount(), is(5));
        assertThat(retryRecord.retryInterval(), is(Period.minutes(30).plusHours(2)));
    }

    @Test
    public void shouldLoadJsonWithMultipleRetries() {
        String retryScheduleName = "retry-every-10Days";
        RetryRecord retryRecord = allRetries.getRetryRecord(retryScheduleName);

        assertThat(retryRecord.name(), is(retryScheduleName));
        assertThat(retryRecord.retryCount(), is(5));
        assertThat(retryRecord.retryInterval(), is(Period.days(10)));
    }

    @Test
    public void shouldAddRetryIdempotently() {
        AllRetries spy = spy(allRetries);

        DateTime startTime = DateTime.now();
        Retry retry = new Retry("retry", "externalId", startTime, 5, Period.days(2));
        spy.createRetry(retry);

        Retry anotherRetry = new Retry("retry", "externalId", startTime, 5, Period.days(2));
        spy.createRetry(anotherRetry);

        Mockito.verify(spy, times(1)).add(Matchers.<Retry>any());
    }

    @After
    public void tearDown() {
        allRetries.removeAll();
    }

}