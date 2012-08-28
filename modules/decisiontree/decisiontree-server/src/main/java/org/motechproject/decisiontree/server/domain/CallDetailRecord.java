package org.motechproject.decisiontree.server.domain;

import org.ektorp.support.TypeDiscriminator;
import org.motechproject.model.MotechBaseDataObject;
import org.motechproject.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.motechproject.util.DateUtil.newDateTime;
import static org.motechproject.util.DateUtil.setTimeZone;

/**
 * Call Detail Record represents call events and data captured in a call along with call metrics.
 */
@TypeDiscriminator("doc.type === 'CallDetailRecord'")
public class CallDetailRecord extends MotechBaseDataObject {

    /**
     * Call status
     */
    public enum Disposition {
        ANSWERED, BUSY, FAILED, NO_ANSWER, UNKNOWN;
    }

    // TODO: replace with DateTime
    private Date startDate;
    private Date endDate;
    private Date answerDate;
    private Disposition disposition;
    private Integer duration;
    private String errorMessage;
    private String phoneNumber;
    private String callId;

    private CallDirection callDirection;
    private List<CallEvent> callEvents = new ArrayList<CallEvent>();

    private Map<String, Object> customProperties = new HashMap<>();

    private CallDetailRecord() {
    }

    public CallDetailRecord(String callId, String phoneNumber) {
        this.callId = callId;
        this.phoneNumber = phoneNumber;
        setStartDate(DateUtil.now().toDate());
    }

    /**
     * Constructor to create CallDetailRecord
     *
     * @param startDate
     * @param endDate
     * @param answerDate
     * @param disposition
     * @param duration
     */
    public CallDetailRecord(Date startDate, Date endDate, Date answerDate, Disposition disposition, Integer duration) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.answerDate = answerDate;
        this.disposition = disposition;
        this.duration = duration;
    }

    /**
     * CallDetailRecord constructor for failed calls
     *
     * @param disposition: Status of call
     * @param errorMessage
     */
    public CallDetailRecord(Disposition disposition, String errorMessage) {
        this.errorMessage = errorMessage;
        this.disposition = disposition;
    }

    /**
     * Creates a call details record for given phone number and call details
     *
     * @param phoneNumber:   phone number of user.
     * @param callDirection: Incoming/outgoing
     * @param disposition:   Call status (busy, failed etc)
     * @return
     */
    public static CallDetailRecord create(String phoneNumber, CallDirection callDirection, Disposition disposition) {
        CallDetailRecord callDetailRecord = new CallDetailRecord();
        callDetailRecord.startDate = DateUtil.now().toDate();
        callDetailRecord.disposition = disposition;
        callDetailRecord.answerDate = callDetailRecord.startDate;
        callDetailRecord.phoneNumber = phoneNumber;
        callDetailRecord.callDirection = callDirection;
        return callDetailRecord;
    }

    public Date getStartDate() {
        return startDate != null ? setTimeZone(newDateTime(startDate)).toDate() : startDate;
    }

    public Date getEndDate() {
        return endDate != null ? setTimeZone(newDateTime(endDate)).toDate() : endDate;
    }

    public Date getAnswerDate() {
        return answerDate != null ? setTimeZone(newDateTime(answerDate)).toDate() : answerDate;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public CallDirection getCallDirection() {
        return callDirection;
    }


    public List<CallEvent> getCallEvents() {
        return callEvents;
    }

    public CallDetailRecord setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public CallDetailRecord setCallEvents(List<CallEvent> callEvents) {
        this.callEvents = callEvents;
        return this;
    }

    public CallDetailRecord setCallDirection(CallDirection callDirection) {
        this.callDirection = callDirection;
        return this;
    }

    public CallDetailRecord setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public CallDetailRecord setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public CallDetailRecord setAnswerDate(Date answerDate) {
        this.answerDate = answerDate;
        return this;
    }

    public CallDetailRecord setDisposition(Disposition disposition) {
        this.disposition = disposition;
        return this;
    }

    public CallDetailRecord setDuration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public CallDetailRecord setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    public CallDetailRecord setCustomProperties(Map<String, Object> customProperties) {
        if (customProperties != null) {
            this.customProperties = customProperties;
        }
        return this;
    }

    public CallDetailRecord addCustomProperty(String key, Object value) {
        this.customProperties.put(key, value);
        return this;
    }

    public String getCallId() {
        return callId;
    }

    public CallDetailRecord setCallId(String callId) {
        this.callId = callId;
        return this;
    }

    /**
     * Adds IVR events such as key press, hangup etc., to current call detail record.
     *
     * @param callEvent
     */
    public CallDetailRecord addCallEvent(CallEvent callEvent) {
        callEvents.add(callEvent);
        return this;
    }

    /**
     * Get last call event for current call.
     *
     * @return Call event
     */
    public CallEvent lastCallEvent() {
        return callEvents.size() == 0 ? null : callEvents.get(callEvents.size() - 1);
    }
}
