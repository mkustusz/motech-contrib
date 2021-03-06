package org.motechproject.ivr.kookoo.service;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.motechproject.event.EventRelay;
import org.motechproject.event.MotechEvent;
import org.motechproject.ivr.event.CallEvent;
import org.motechproject.ivr.event.IVREvent;
import org.motechproject.ivr.kookoo.KookooIVRResponseBuilder;
import org.motechproject.ivr.kookoo.domain.KookooCallDetailRecord;
import org.motechproject.ivr.kookoo.eventlogging.CallEventConstants;
import org.motechproject.ivr.kookoo.repository.AllKooKooCallDetailRecords;
import org.motechproject.ivr.model.CallDetailRecord;
import org.motechproject.ivr.model.CallDirection;
import org.motechproject.scheduler.context.EventContext;
import org.motechproject.util.DateUtil;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({DateUtil.class, EventContext.class})
@RunWith(PowerMockRunner.class)
public class KookooCallDetailRecordsServiceImplTest {

    @Mock
    private AllKooKooCallDetailRecords allKooKooCallDetailRecords;
    @Mock
    private EventContext eventContext;
    @Mock
    private EventRelay eventRelay;

    private KookooCallDetailRecordsServiceImpl kookooCallDetailRecordsService;
    private KookooCallDetailRecord kookooCallDetailRecord;

    private final String callDetailRecordId = "callId";
    private DateTime now;

    @Before
    public void setUp() {
        initMocks(this);

        mockStatic(EventContext.class);
        mockStatic(DateUtil.class);
        when(EventContext.getInstance()).thenReturn(eventContext);
        when(eventContext.getEventRelay()).thenReturn(eventRelay);
        now = new DateTime(2011, 1, 1, 10, 25, 30, 0);
        when(DateUtil.now()).thenReturn(now);
        when(DateUtil.newDateTime(now.toDate())).thenReturn(now);
        when(DateUtil.setTimeZone(now)).thenReturn(now.toDateTime(DateTimeZone.forTimeZone(Calendar.getInstance().getTimeZone())));

        kookooCallDetailRecordsService = new KookooCallDetailRecordsServiceImpl(allKooKooCallDetailRecords, allKooKooCallDetailRecords);
        CallDetailRecord callDetailRecord = CallDetailRecord.create("85437", CallDirection.Inbound, CallDetailRecord.Disposition.ANSWERED);
        kookooCallDetailRecord = new KookooCallDetailRecord(callDetailRecord, "fdsfdsf");
        Mockito.when(allKooKooCallDetailRecords.get(callDetailRecordId)).thenReturn(kookooCallDetailRecord);
    }

    @Test
    public void shouldUpdateTheEndDateWhenClosingCallDetailRecord() {
        kookooCallDetailRecordsService.close("callId", "externalId", new CallEvent(IVREvent.GotDTMF.toString()));
        verify(allKooKooCallDetailRecords).update(kookooCallDetailRecord);
    }

    @Test
    public void shouldRaiseEventWhenClosingCallDetailRecord() {
        kookooCallDetailRecordsService.close("callId", "externalId", new CallEvent(IVREvent.GotDTMF.toString()));

        ArgumentCaptor<MotechEvent> eventCaptor = ArgumentCaptor.forClass(MotechEvent.class);
        verify(eventRelay).sendEventMessage(eventCaptor.capture());

        MotechEvent event = eventCaptor.getValue();
        assertEquals(KookooCallDetailRecordsServiceImpl.CLOSE_CALL_SUBJECT, event.getSubject());
        assertEquals("callId", event.getParameters().get(KookooCallDetailRecordsServiceImpl.CALL_ID));
        assertEquals("externalId", event.getParameters().get(KookooCallDetailRecordsServiceImpl.EXTERNAL_ID));
    }

    @Test
    public void shouldSetAnsweredDate_WhenSettingCallAsAnswered() {
        String vendorCallId = "callId";
        String callDetailRecordId = "callDetailRecordId";
        CallDetailRecord callDetailRecord = CallDetailRecord.create("85437", CallDirection.Inbound, CallDetailRecord.Disposition.UNKNOWN);
        kookooCallDetailRecord = new KookooCallDetailRecord(callDetailRecord, null);
        when(allKooKooCallDetailRecords.get(callDetailRecordId)).thenReturn(kookooCallDetailRecord);

        kookooCallDetailRecordsService.setCallRecordAsAnswered(vendorCallId, callDetailRecordId);

        assertEquals(vendorCallId, kookooCallDetailRecord.getVendorCallId());
        assertEquals(now.toDate(), callDetailRecord.getAnswerDate());
        assertEquals(CallDetailRecord.Disposition.ANSWERED, callDetailRecord.getDisposition());
        verify(allKooKooCallDetailRecords).update(kookooCallDetailRecord);
    }

    @Test
    public void shouldSetAsNotAnswered_WhenCallNotAnswered() {
        String vendorCallId = "callId";
        String callDetailRecordId = "callDetailRecordId";
        CallDetailRecord callDetailRecord = CallDetailRecord.create("85437", CallDirection.Inbound, CallDetailRecord.Disposition.UNKNOWN);
        kookooCallDetailRecord = new KookooCallDetailRecord(callDetailRecord, vendorCallId);
        when(allKooKooCallDetailRecords.get(callDetailRecordId)).thenReturn(kookooCallDetailRecord);

        kookooCallDetailRecordsService.setCallRecordAsNotAnswered(callDetailRecordId);

        assertEquals(CallDetailRecord.Disposition.NO_ANSWER, callDetailRecord.getDisposition());
        verify(allKooKooCallDetailRecords).update(kookooCallDetailRecord);

    }

    @Test
    public void appendEvent() {
        assertEquals(0, kookooCallDetailRecord.getCallDetailRecord().getCallEvents().size());
        kookooCallDetailRecordsService.appendEvent(callDetailRecordId, IVREvent.NewCall, null);
        assertEquals(1, kookooCallDetailRecord.getCallDetailRecord().getCallEvents().size());
    }

    @Test
    public void appendEventShouldAddTheEventIfUserInput_IsNotEmpty() {
        String dtmfInput = "2";
        kookooCallDetailRecordsService.appendEvent(callDetailRecordId, IVREvent.GotDTMF, dtmfInput);
        List<CallEvent> callEvents = kookooCallDetailRecord.getCallDetailRecord().getCallEvents();
        assertEquals(1, callEvents.size());
        assertEquals(dtmfInput, callEvents.get(0).getData().getFirst(CallEventConstants.DTMF_DATA));
    }

    @Test
    public void appendLastCallEvent() {
        kookooCallDetailRecordsService.appendEvent(callDetailRecordId, IVREvent.GotDTMF, "1");
        KookooIVRResponseBuilder ivrResponseBuilder = new KookooIVRResponseBuilder();
        ivrResponseBuilder.withPlayAudios("abcd");
        String response = "gdfgfdgfdg";
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(CallEventConstants.CUSTOM_DATA_LIST, response);

        kookooCallDetailRecordsService.appendToLastCallEvent(callDetailRecordId, map);
        List<CallEvent> callEvents = kookooCallDetailRecord.getCallDetailRecord().getCallEvents();
        assertEquals(1, callEvents.size());
        assertEquals(response, callEvents.get(0).getData().getFirst(CallEventConstants.CUSTOM_DATA_LIST));
    }

    @Test
    public void scenario1() {
        kookooCallDetailRecordsService.appendEvent(callDetailRecordId, IVREvent.GotDTMF, "1");
        String response = "asada";
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(CallEventConstants.CUSTOM_DATA_LIST, response);
        kookooCallDetailRecordsService.appendToLastCallEvent(callDetailRecordId, map);
        List<CallEvent> callEvents = kookooCallDetailRecord.getCallDetailRecord().getCallEvents();
        assertEquals(1, callEvents.size());
        assertEquals(response, callEvents.get(0).getData().getFirst(CallEventConstants.CUSTOM_DATA_LIST));
    }

    @Test
    public void create() {
        kookooCallDetailRecordsService.createAnsweredRecord("1111", "2222", CallDirection.Inbound);
        ArgumentCaptor<KookooCallDetailRecord> capture = ArgumentCaptor.forClass(KookooCallDetailRecord.class);
        verify(allKooKooCallDetailRecords).add(capture.capture());
        KookooCallDetailRecord newKookooCallDetailRecord = capture.getValue();
        assertEquals(0, newKookooCallDetailRecord.getCallDetailRecord().getCallEvents().size());
    }
}
