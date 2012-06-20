package org.motechproject.retry;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.retry.dao.AllRetries;
import org.motechproject.retry.domain.Retry;
import org.motechproject.retry.domain.RetryRequest;
import org.motechproject.retry.service.RetryServiceImpl;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-retry.xml")
public class RetryServiceIT {
    @Autowired
    private RetryServiceImpl retryServiceImpl;
    @Autowired
    private AllRetries allRetries;
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Test
    public void shouldCreateRetryEvent() {
        String groupName = "campaign-retries";
        String name = "retry-every-2hrs-and-30mins";
        String externalId = "externalId";
        DateTime referenceTime = DateTime.now();

        retryServiceImpl.schedule(new RetryRequest(name, externalId, referenceTime));

        Retry activeRetry = allRetries.getActiveRetry(externalId, name);
        assertThat(activeRetry.hasPendingRetires(), is(true));
        assertThat(activeRetry.startTime(), is(referenceTime.plus(Period.minutes(30))));

        retryServiceImpl.fulfill(externalId, groupName);

        assertNull(allRetries.getActiveRetry(externalId, name));
    }

    @After
    public void tearDown() {
        allRetries.removeAll();
        removeQuartzJobs();
    }

    private void removeQuartzJobs() {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            List<String> jobGroupNames = scheduler.getJobGroupNames();
            for (String groupName : jobGroupNames) {
                Set<JobKey> jobNames = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
                for (JobKey jobKey : jobNames)
                    scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}
