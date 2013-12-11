package org.motechproject.messagecampaign.service;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.config.core.constants.ConfigurationConstants;
import org.motechproject.config.service.ConfigurationService;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link MessageCampaignService}
 */
@Service("messageCampaignService")
public class MessageCampaignServiceImpl implements MessageCampaignService {

    private CampaignEnrollmentService campaignEnrollmentService;
    private CampaignEnrollmentRecordMapper campaignEnrollmentRecordMapper;
    private AllCampaignEnrollments allCampaignEnrollments;
    private CampaignSchedulerFactory campaignSchedulerFactory;
    private AllMessageCampaigns allMessageCampaigns;
    private EventRelay relay;
    @Autowired
    @Qualifier("messageCampaignSettings")
    private SettingsFacade settingsFacade;

    @Autowired
    private CommonsMultipartResolver commonsMultipartResolver;

    @Autowired
    private ConfigurationService configurationService;

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
        CampaignEnrollment enrollment = new CampaignEnrollment(request.externalId(), request.campaignName()).setReferenceDate(request.referenceDate()).setDeliverTime(request.deliverTime());
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
                .setReferenceDate(enrollRequest.referenceDate());
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
    public void campaignCompleted(String externalId, String campaignName) {
        stopAll(new CampaignRequest(externalId, campaignName, null, null));
    }

    @PostConstruct
    @Override
    public void loadCampaigns() {
        InputStream inputStream = settingsFacade.getRawConfig(MESSAGE_CAMPAIGNS_JSON_FILENAME);
        if (inputStream != null) {
            List<CampaignRecord> records = new CampaignJsonLoader().loadCampaigns(inputStream);
            for (CampaignRecord record : records) {
                allMessageCampaigns.saveOrUpdate(record);
            }
        }
    }

    @MotechListener(subjects = ConfigurationConstants.FILE_CHANGED_EVENT_SUBJECT)
    public void changeMaxUploadSize(MotechEvent event) {
        String uploadSize =  configurationService.getPlatformSettings().getUploadSize();

        if (StringUtils.isNotBlank(uploadSize)) {
            commonsMultipartResolver.setMaxUploadSize(Long.valueOf(uploadSize));
        }
    }
}
