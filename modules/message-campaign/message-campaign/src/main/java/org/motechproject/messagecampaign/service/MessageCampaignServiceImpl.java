package org.motechproject.messagecampaign.service;

import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.messagecampaign.EventKeys;
import org.motechproject.messagecampaign.contract.CampaignRequest;
import org.motechproject.messagecampaign.dao.AllCampaignEnrollments;
import org.motechproject.messagecampaign.dao.AllMessageCampaigns;
import org.motechproject.messagecampaign.domain.CampaignNotFoundException;
import org.motechproject.messagecampaign.domain.campaign.CampaignEnrollment;
import org.motechproject.messagecampaign.loader.CampaignJsonLoader;
import org.motechproject.messagecampaign.scheduler.CampaignSchedulerFactory;
import org.motechproject.messagecampaign.scheduler.CampaignSchedulerService;
import org.motechproject.messagecampaign.userspecified.CampaignRecord;
import org.motechproject.messagecampaign.web.ex.EnrollmentNotFoundException;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("messageCampaignService")
public class MessageCampaignServiceImpl implements MessageCampaignService {

    public static final String MESSAGE_CAMPAIGNS_JSON_FILENAME = "message-campaigns.json";

    private CampaignEnrollmentService campaignEnrollmentService;
    private CampaignEnrollmentRecordMapper campaignEnrollmentRecordMapper;
    private AllCampaignEnrollments allCampaignEnrollments;
    private CampaignSchedulerFactory campaignSchedulerFactory;
    private AllMessageCampaigns allMessageCampaigns;
    private EventRelay relay;
    @Autowired
    private SettingsFacade settingsFacade;

    @Autowired
    public MessageCampaignServiceImpl(CampaignEnrollmentService campaignEnrollmentService, CampaignEnrollmentRecordMapper campaignEnrollmentRecordMapper, AllCampaignEnrollments allCampaignEnrollments, CampaignSchedulerFactory campaignSchedulerFactory,
                                      AllMessageCampaigns allMessageCampaigns, EventRelay relay) {
        this.campaignEnrollmentService = campaignEnrollmentService;
        this.campaignEnrollmentRecordMapper = campaignEnrollmentRecordMapper;
        this.allCampaignEnrollments = allCampaignEnrollments;
        this.campaignSchedulerFactory = campaignSchedulerFactory;
        this.allMessageCampaigns = allMessageCampaigns;
        this.relay = relay;
    }

    public void startFor(CampaignRequest request) {
        CampaignEnrollment enrollment = new CampaignEnrollment(request.externalId(), request.campaignName()).setReferenceDate(request.referenceDate()).setReferenceTime(request.referenceTime()).setDeliverTime(request.deliverTime());
        campaignEnrollmentService.register(enrollment);
        CampaignSchedulerService campaignScheduler = campaignSchedulerFactory.getCampaignScheduler(request.campaignName());
        campaignScheduler.start(enrollment);

        Map<String, Object> param = new HashMap<>();
        param.put(EventKeys.EXTERNAL_ID_KEY, enrollment.getExternalId());
        param.put(EventKeys.CAMPAIGN_NAME_KEY, enrollment.getCampaignName());
        MotechEvent event = new MotechEvent(EventKeys.ENROLLED_USER_SUBJECT, param);

        relay.sendEventMessage(event);
    }

    @Override
    public void stopAll(CampaignRequest request) {
        campaignEnrollmentService.unregister(request.externalId(), request.campaignName());
        CampaignEnrollment enrollment = allCampaignEnrollments.findByExternalIdAndCampaignName(request.externalId(), request.campaignName());
        campaignSchedulerFactory.getCampaignScheduler(request.campaignName()).stop(enrollment);

        Map<String, Object> param = new HashMap<>();
        param.put(EventKeys.EXTERNAL_ID_KEY, request.externalId());
        param.put(EventKeys.CAMPAIGN_NAME_KEY, request.campaignName());
        MotechEvent event = new MotechEvent(EventKeys.UNENROLLED_USER_SUBJECT, param);

        relay.sendEventMessage(event);
    }

    @Override
    public List<CampaignEnrollmentRecord> search(CampaignEnrollmentsQuery query) {
        List<CampaignEnrollmentRecord> campaignEnrollmentRecords = new ArrayList<>();
        for (CampaignEnrollment campaignEnrollment : campaignEnrollmentService.search(query)) {
            campaignEnrollmentRecords.add(campaignEnrollmentRecordMapper.map(campaignEnrollment));
        }
        return campaignEnrollmentRecords;
    }

    @Override
    public Map<String, List<DateTime>> getCampaignTimings(String externalId, String campaignName, DateTime startDate, DateTime endDate) {
        CampaignEnrollment enrollment = allCampaignEnrollments.findByExternalIdAndCampaignName(externalId, campaignName);
        if (!enrollment.isActive()) {
            return new HashMap<>();
        }
        return campaignSchedulerFactory.getCampaignScheduler(campaignName).getCampaignTimings(startDate, endDate, enrollment);
    }

    @Override
    public void updateEnrollment(CampaignRequest enrollRequest, String enrollmentId) {
        CampaignEnrollment existingEnrollment = allCampaignEnrollments.get(enrollmentId);

        if (existingEnrollment == null) {
            throw new EnrollmentNotFoundException("Enrollment with id " + enrollmentId + " not found");
        } else {
            CampaignEnrollment byIdAndName = allCampaignEnrollments.findByExternalIdAndCampaignName(
                    enrollRequest.externalId(), enrollRequest.campaignName());
            if (byIdAndName != null && !byIdAndName.getId().equals(enrollmentId)) {
                throw new IllegalArgumentException(String.format("%s is already enrolled in %s campaign, enrollmentId: %s",
                        enrollRequest.externalId(), enrollRequest.campaignName(), byIdAndName.getId()));
            }
        }

        campaignSchedulerFactory.getCampaignScheduler(existingEnrollment.getCampaignName()).stop(existingEnrollment);

        existingEnrollment.setExternalId(enrollRequest.externalId()).setDeliverTime(enrollRequest.deliverTime())
                .setReferenceDate(enrollRequest.referenceDate()).setReferenceTime(enrollRequest.referenceTime());
        allCampaignEnrollments.saveOrUpdate(existingEnrollment);

        campaignSchedulerFactory.getCampaignScheduler(existingEnrollment.getCampaignName()).start(existingEnrollment);
    }

    @Override
    public void stopAll(CampaignEnrollmentsQuery query) {
        List<CampaignEnrollment> enrollments = campaignEnrollmentService.search(query);
        for (CampaignEnrollment enrollment : enrollments) {
            campaignEnrollmentService.unregister(enrollment.getExternalId(), enrollment.getCampaignName());
            campaignSchedulerFactory.getCampaignScheduler(enrollment.getCampaignName()).stop(enrollment);
        }
    }

    @Override
    public void saveCampaign(CampaignRecord campaign) {
        allMessageCampaigns.saveOrUpdate(campaign);
    }

    @Override
    public void deleteCampaign(String campaignName) {
        CampaignRecord campaignRecord = allMessageCampaigns.findFirstByName(campaignName);

        if (campaignRecord == null) {
            throw new CampaignNotFoundException("Campaign not found: " + campaignName);
        } else {
            CampaignEnrollmentsQuery enrollmentsQuery = new CampaignEnrollmentsQuery().withCampaignName(campaignName);
            stopAll(enrollmentsQuery);

            allMessageCampaigns.remove(campaignRecord);
        }
    }

    @Override
    public CampaignRecord getCampaignRecord(String campaignName) {
        return allMessageCampaigns.findFirstByName(campaignName);
    }

    @Override
    public List<CampaignRecord> getAllCampaignRecords() {
        return allMessageCampaigns.getAll();
    }

    @Override
    public void campaignCompleted(Map<String, Object> parameters) {
        CampaignRequest request = new CampaignRequest();
        request.setExternalId((String) parameters.get(EventKeys.EXTERNAL_ID_KEY));
        request.setCampaignName((String) parameters.get(EventKeys.CAMPAIGN_NAME_KEY));

        stopAll(request);
    }

    @PostConstruct
    void loadCampaigns() {
        InputStream inputStream = settingsFacade.getRawConfig(MESSAGE_CAMPAIGNS_JSON_FILENAME);
        if (inputStream != null) {
            List<CampaignRecord> records = new CampaignJsonLoader().loadCampaigns(inputStream);
            for (CampaignRecord record : records) {
                allMessageCampaigns.saveOrUpdate(record);
            }
        }
    }
}
