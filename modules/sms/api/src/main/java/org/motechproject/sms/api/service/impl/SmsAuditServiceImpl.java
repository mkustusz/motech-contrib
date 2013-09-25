package org.motechproject.sms.api.service.impl;

import org.motechproject.sms.api.domain.SmsRecord;
import org.motechproject.sms.api.repository.AllSmsRecords;
import org.motechproject.sms.api.domain.SmsRecords;
import org.motechproject.sms.api.service.SmsAuditService;
import org.motechproject.sms.api.service.SmsRecordSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsAuditServiceImpl implements SmsAuditService {

    private AllSmsRecords allSmsRecords;

    @Autowired
    public SmsAuditServiceImpl(AllSmsRecords allSmsRecords) {
        this.allSmsRecords = allSmsRecords;
    }

    @Override
    public void log(SmsRecord smsRecord) {
        allSmsRecords.addOrReplace(smsRecord);
    }

    @Override
    public void updateDeliveryStatus(String recipient, String refNo, String deliveryStatus) {
        allSmsRecords.updateDeliveryStatus(recipient, refNo, deliveryStatus);
    }

    public List<SmsRecord> findAllSmsRecords() {
        return allSmsRecords.getAll();
    }

    @Override
    public SmsRecords findAllSmsRecords(SmsRecordSearchCriteria criteria) {
        return allSmsRecords.findAllBy(criteria);
    }
}
