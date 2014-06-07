package org.motechproject.messagecampaign.scheduler;

import org.joda.time.LocalDate;
import org.motechproject.event.MotechEvent;
import org.motechproject.messagecampaign.EventKeys;
import org.motechproject.messagecampaign.dao.AllMessageCampaigns;
import org.motechproject.messagecampaign.domain.campaign.CampaignEnrollment;
import org.motechproject.messagecampaign.domain.campaign.CronBasedCampaign;
import org.motechproject.messagecampaign.domain.message.CampaignMessage;
import org.motechproject.messagecampaign.domain.message.CronBasedCampaignMessage;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CronBasedCampaignSchedulerService extends CampaignSchedulerService<CronBasedCampaignMessage, CronBasedCampaign> {

    @Autowired
    public CronBasedCampaignSchedulerService(MotechSchedulerService schedulerService, AllMessageCampaigns allMessageCampaigns) {
        super(schedulerService, allMessageCampaigns);
    }

    @Override
    protected void scheduleMessageJob(CampaignEnrollment enrollment, CampaignMessage message) {
        CronBasedCampaignMessage cronMessage = (CronBasedCampaignMessage) message;
        MotechEvent motechEvent = new MotechEvent(EventKeys.SEND_MESSAGE, jobParams(message.messageKey(), enrollment));
        LocalDate startDate = enrollment.getReferenceDate();
        CronSchedulableJob schedulableJob = new CronSchedulableJob(motechEvent, cronMessage.cron(), startDate.toDate(), null);
        getSchedulerService().scheduleJob(schedulableJob);
    }

    @Override
    public void stop(CampaignEnrollment enrollment) {
        CronBasedCampaign campaign = (CronBasedCampaign) getAllMessageCampaigns().getCampaign(enrollment.getCampaignName());
        for (CronBasedCampaignMessage message : campaign.getMessages()) {
            getSchedulerService().safeUnscheduleJob(EventKeys.SEND_MESSAGE, messageJobIdFor(message.messageKey(), enrollment.getExternalId(), enrollment.getCampaignName()));
        }
    }
}
