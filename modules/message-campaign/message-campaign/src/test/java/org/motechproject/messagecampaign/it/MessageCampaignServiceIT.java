package org.motechproject.messagecampaign.it;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.messagecampaign.contract.CampaignRequest;
import org.motechproject.messagecampaign.service.MessageCampaignService;
import org.motechproject.scheduler.factory.MotechSchedulerFactoryBean;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.quartz.TriggerKey.triggerKey;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:testMessageCampaignApplicationContext.xml")
public class MessageCampaignServiceIT {

    @Autowired
    MessageCampaignService messageCampaignService;

    @Autowired
    MotechSchedulerFactoryBean motechSchedulerFactoryBean;

    Scheduler scheduler;

    @Before
    public void setup() {
        scheduler = motechSchedulerFactoryBean.getQuartzScheduler();
    }

    @Test
    public void shouldUnscheduleMessageJobsWhenCampaignIsStopped() throws SchedulerException {
        CampaignRequest campaignRequest = new CampaignRequest("entity_1", "PREGNANCY", new LocalDate(2020, 7, 10), null);

        TriggerKey triggerKey = triggerKey("org.motechproject.messagecampaign.fired-campaign-message-MessageJob.PREGNANCY.entity_1.PREGNANCY", "default");

        messageCampaignService.enroll(campaignRequest);
        assertTrue(scheduler.checkExists(triggerKey));

        messageCampaignService.unenroll(campaignRequest.externalId(), campaignRequest.campaignName());
        assertFalse(scheduler.checkExists(triggerKey));
    }
}
