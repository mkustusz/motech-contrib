package org.motechproject.messagecampaign.scheduler;

import org.joda.time.DateTime;
import org.motechproject.commons.date.model.Time;
import org.motechproject.messagecampaign.EventKeys;
import org.motechproject.messagecampaign.builder.SchedulerPayloadBuilder;
import org.motechproject.messagecampaign.dao.AllMessageCampaigns;
import org.motechproject.messagecampaign.domain.campaign.Campaign;
import org.motechproject.messagecampaign.domain.campaign.CampaignEnrollment;
import org.motechproject.messagecampaign.domain.message.CampaignMessage;
import org.motechproject.messagecampaign.scheduler.exception.CampaignEnrollmentException;
import org.motechproject.scheduler.MotechSchedulerService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for handling campaign (un)scheduling
 * @param <M> Type of {@link CampaignMessage}
 * @param <C> Type of {@link Campaign}
 */
public abstract class CampaignSchedulerService<M extends CampaignMessage, C extends Campaign<M>> {

    private MotechSchedulerService schedulerService;
    private AllMessageCampaigns allMessageCampaigns;
    private JobIdFactory jobIdFactory;

    protected CampaignSchedulerService(MotechSchedulerService schedulerService, AllMessageCampaigns allMessageCampaigns) {
        this.schedulerService = schedulerService;
        this.allMessageCampaigns = allMessageCampaigns;
        jobIdFactory = new JobIdFactory();
    }

    public void start(CampaignEnrollment enrollment) {
        C campaign = (C) allMessageCampaigns.getCampaign(enrollment.getCampaignName());
        for (M message : campaign.getMessages()) {
            scheduleMessageJob(enrollment, message);
        }
    }

    public abstract void stop(CampaignEnrollment enrollment);

    public Map<String, List<DateTime>> getCampaignTimings(DateTime startDate, DateTime endDate, CampaignEnrollment enrollment) {
        Map<String, List<DateTime>> messageTimingsMap = new HashMap<>();
        C campaign = (C) allMessageCampaigns.getCampaign(enrollment.getCampaignName());
        for (M message : campaign.getMessages()) {
            String externalJobIdPrefix = messageJobIdFor(message.messageKey(), enrollment.getExternalId(), enrollment.getCampaignName());
            List<DateTime> dates = convertToDateTimeList(schedulerService.getScheduledJobTimingsWithPrefix(EventKeys.SEND_MESSAGE, externalJobIdPrefix, startDate.toDate(), endDate.toDate()));

            messageTimingsMap.put(message.name(), dates);
        }
        return messageTimingsMap;
    }

    protected abstract void scheduleMessageJob(CampaignEnrollment enrollment, CampaignMessage message);

    protected Time deliverTimeFor(CampaignEnrollment enrollment, CampaignMessage message) throws CampaignEnrollmentException {
        Time deliveryTime = enrollment.getDeliverTime() != null ? enrollment.getDeliverTime() : message.getStartTime();

        if (deliveryTime == null) {
            throw new CampaignEnrollmentException(String.format("Cannot enroll %s for message campaign %s - Start time not defined for campaign. Define it in campaign-message.json or at enrollment time", enrollment.getExternalId(), message.name()));
        }
        return deliveryTime;
    }

    protected Map<String, Object> jobParams(String messageKey, CampaignEnrollment enrollment) {
        Campaign campaign = allMessageCampaigns.getCampaign(enrollment.getCampaignName());
        return new SchedulerPayloadBuilder()
                .withJobId(messageJobIdFor(messageKey, enrollment.getExternalId(), enrollment.getCampaignName()))
                .withCampaignName(campaign.getName())
                .withMessageKey(messageKey)
                .withExternalId(enrollment.getExternalId())
                .payload();
    }

    protected String messageJobIdFor(String messageKey, String externalId, String campaignName) {
        return jobIdFactory.getMessageJobIdFor(campaignName, externalId, messageKey);
    }

    public MotechSchedulerService getSchedulerService() {
        return schedulerService;
    }

    public AllMessageCampaigns getAllMessageCampaigns() {
        return allMessageCampaigns;
    }

    private List<DateTime> convertToDateTimeList(final List<Date> dates) {
        List<DateTime> list = new ArrayList<>(dates.size());

        for (Date date : dates) {
            list.add(new DateTime(date));
        }

        return list;
    }
}

