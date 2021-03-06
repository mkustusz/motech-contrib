package org.motechproject.calllog.it;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.ReplaceWithMock;
import org.motechproject.event.MotechEvent;
import org.motechproject.calllog.handler.CallLogHandler;
import org.motechproject.calllog.request.CallLogRequest;
import org.motechproject.calllog.request.OutboundDetails;
import org.motechproject.calllog.service.CallLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.motechproject.calllog.handler.EventKeys.CALL_LOG_RECEIVED;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:/test-applicationCallLogContext.xml")
public class CallLogHandlerIT {

    @Autowired
    private CallLogHandler callLogHandler;
    @ReplaceWithMock
    private CallLogService callLogService;

    @Rule
    public ExpectedException exceptionThrown = ExpectedException.none();

    @Test
    public void shouldValidateCallLogRequest() throws Exception {
        exceptionThrown.expect(RuntimeException.class);
        exceptionThrown.expectMessage("field:outboundDetails:may not be null");
        exceptionThrown.expectMessage("field:disposition:may not be null");
        exceptionThrown.expectMessage("field:phoneNumber:may not be null");
        exceptionThrown.expectMessage("field:disposition:invalid value");
        exceptionThrown.expectMessage("field:callId:may not be null");

        CallLogRequest callLogRequest = new CallLogRequest();
        Map<String, Object> params = new HashMap<>();
        params.put("0", callLogRequest);
        MotechEvent motechEvent = new MotechEvent(CALL_LOG_RECEIVED, params);

        callLogHandler.handleCallLogReceived(motechEvent);

        verify(callLogService, never()).add(callLogRequest);
    }

    @Test
    public void shouldValidateDateFormatCallLogRequest() throws Exception {
        exceptionThrown.expect(RuntimeException.class);
        exceptionThrown.expectMessage("field:startTime:invalid date format");
        exceptionThrown.expectMessage("field:outboundDetails.attempt:may not be null");
        exceptionThrown.expectMessage("field:outboundDetails.callType:may not be null");
        exceptionThrown.expectMessage("field:disposition:may not be null");
        exceptionThrown.expectMessage("field:disposition:invalid value");
        exceptionThrown.expectMessage("field:outboundDetails.requestId:may not be null");
        exceptionThrown.expectMessage("field:outboundDetails.attemptTime:invalid date format");

        CallLogRequest callLogRequest = new CallLogRequest();
        callLogRequest.setCallId("callid");
        callLogRequest.setPhoneNumber("123456789");
        callLogRequest.setStartTime("12/12/12");
        callLogRequest.setEndTime("16/12/2013 00:00:00");
        OutboundDetails outboundDetails = new OutboundDetails();
        outboundDetails.setAttemptTime("not a date");
        callLogRequest.setOutboundDetails(outboundDetails);

        Map<String, Object> params = new HashMap<>();
        params.put("0", callLogRequest);
        MotechEvent motechEvent = new MotechEvent(CALL_LOG_RECEIVED, params);

        callLogHandler.handleCallLogReceived(motechEvent);

        verify(callLogService, never()).add(callLogRequest);
    }

    @Test
    public void shouldValidateDispositionTypeCallLogRequest() throws Exception {
        exceptionThrown.expect(RuntimeException.class);
        exceptionThrown.expectMessage("field:disposition:invalid value");
        exceptionThrown.expectMessage("field:outboundDetails:may not be null");

        CallLogRequest callLogRequest = new CallLogRequest();
        callLogRequest.setCallId("callid");
        callLogRequest.setPhoneNumber("123456789");
        callLogRequest.setStartTime("16/12/2013 00:00:00");
        callLogRequest.setEndTime("16/12/2013 00:00:00");
        callLogRequest.setDisposition("something");

        Map<String, Object> params = new HashMap<>();
        params.put("0", callLogRequest);
        MotechEvent motechEvent = new MotechEvent(CALL_LOG_RECEIVED, params);

        callLogHandler.handleCallLogReceived(motechEvent);

        verify(callLogService, never()).add(callLogRequest);
    }
}