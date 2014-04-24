package org.motechproject.messagecampaign.it.scheduler;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.commons.date.model.Time;
import org.motechproject.messagecampaign.contract.CampaignRequest;
import org.motechproject.messagecampaign.service.MessageCampaignService;
import org.motechproject.scheduler.factory.MotechSchedulerFactoryBean;
import org.motechproject.scheduler.MotechSchedulerService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.motechproject.commons.date.util.DateUtil.newDateTime;
import static org.motechproject.testing.utils.TimeFaker.fakeToday;
import static org.motechproject.testing.utils.TimeFaker.stopFakingTime;
import static org.quartz.TriggerKey.triggerKey;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:day_of_week_campaign_it/context.xml")
public class DayOfWeekCampaignSchedulingIT {

    @Autowired
    MessageCampaignService messageCampaignService;

    @Autowired
    MotechSchedulerFactoryBean motechSchedulerFactoryBean;

    Scheduler scheduler;

    @Autowired
    private MotechSchedulerService schedulerService;

    @Before
    public void setup() {
        scheduler = motechSchedulerFactoryBean.getQuartzScheduler();
    }

    @After
    public void teardown() {
        schedulerService.unscheduleAllJobs("org.motechproject.messagecampaign");
    }

    @Test
    public void shouldScheduleMessageAtItsStartTime() throws SchedulerException {
        CampaignRequest campaignRequest = new CampaignRequest("entity_1", "DayOfWeekCampaign", new LocalDate(2020, 7, 10), null); // Friday
        messageCampaignService.enroll(campaignRequest);
        List<DateTime> fireTimes = getFireTimes("org.motechproject.messagecampaign.fired-campaign-message-MessageJob.DayOfWeekCampaign.entity_1.message_key_1");
        assertEquals(asList(
                newDateTime(2020, 7, 10, 10, 30, 0),
                newDateTime(2020, 7, 13, 10, 30, 0),
                newDateTime(2020, 7, 17, 10, 30, 0),
                newDateTime(2020, 7, 20, 10, 30, 0)),
                fireTimes);
    }

    @Test
    public void shouldScheduleMessageAtUserPreferredTime() throws SchedulerException {
        CampaignRequest campaignRequest = new CampaignRequest("entity_1", "DayOfWeekCampaign", new LocalDate(2020, 7, 10), new Time(8, 20)); // Friday
        messageCampaignService.enroll(campaignRequest);
        List<DateTime> fireTimes = getFireTimes("org.motechproject.messagecampaign.fired-campaign-message-MessageJob.DayOfWeekCampaign.entity_1.message_key_1");
        assertEquals(asList(
                newDateTime(2020, 7, 10, 8, 20, 0),
                newDateTime(2020, 7, 13, 8, 20, 0),
                newDateTime(2020, 7, 17, 8, 20, 0),
                newDateTime(2020, 7, 20, 8, 20, 0)),
                fireTimes);
    }

    @Test
    public void shouldNotScheduleMessagesInThePastForDelayedEnrollment() throws SchedulerException {
        try {
            fakeToday(new LocalDate(2020, 7, 15));
            CampaignRequest campaignRequest = new CampaignRequest("entity_1", "DayOfWeekCampaign", new LocalDate(2020, 7, 10), null); // Friday
            messageCampaignService.enroll(campaignRequest);
            List<DateTime> fireTimes = getFireTimes("org.motechproject.messagecampaign.fired-campaign-message-MessageJob.DayOfWeekCampaign.entity_1.message_key_1");
            assertEquals(asList(
                    newDateTime(2020, 7, 17, 10, 30, 0),
                    newDateTime(2020, 7, 20, 10, 30, 0)),
                    fireTimes);
        } finally {
            stopFakingTime();
        }
    }

    private List<DateTime> getFireTimes(String triggerKey) throws SchedulerException {
        Trigger trigger = scheduler.getTrigger(triggerKey(triggerKey, "default"));
        List<DateTime> fireTimes = new ArrayList<>();
        Date nextFireTime = trigger.getNextFireTime();
        while (nextFireTime != null) {
            fireTimes.add(newDateTime(nextFireTime));
            nextFireTime = trigger.getFireTimeAfter(nextFireTime);
        }
        return fireTimes;
    }
}
