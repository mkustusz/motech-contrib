package org.motechproject.sms.http;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.event.EventRelay;
import org.motechproject.model.MotechEvent;
import org.motechproject.sms.api.constants.EventDataKeys;
import org.motechproject.sms.api.constants.EventSubjects;

import javax.servlet.http.HttpServletRequest;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class InboundSmsControllerTest {


    @Mock
    private EventRelay eventRelay;
    @Mock
    private TemplateReader templateReader;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void handleIncomingSms() {
        SmsHttpTemplate template = new SmsHttpTemplate();
        SmsHttpTemplate.Incoming incoming = new SmsHttpTemplate.Incoming();
        incoming.setMessageKey("message");
        incoming.setSenderKey("sender");
        incoming.setTimestampKey("timestamp");
        template.setIncoming(incoming);
        when(templateReader.getTemplate("/sms-http-template.json")).thenReturn(template);

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getParameter(template.getIncoming().getMessageKey())).thenReturn("some text message");
        when(httpServletRequest.getParameter(template.getIncoming().getSenderKey())).thenReturn("1234567890");
        when(httpServletRequest.getParameter(template.getIncoming().getTimestampKey())).thenReturn("time in some format");

        new InboundSmsController(templateReader, eventRelay).handle(httpServletRequest);

        ArgumentCaptor<MotechEvent> eventCaptor = ArgumentCaptor.forClass(MotechEvent.class);
        verify(eventRelay).sendEventMessage(eventCaptor.capture());

        MotechEvent event = eventCaptor.getValue();
        assertEquals(EventSubjects.INBOUND_SMS, event.getSubject());
        assertEquals("some text message", event.getParameters().get(EventDataKeys.INBOUND_MESSAGE));
        assertEquals("1234567890", event.getParameters().get(EventDataKeys.SENDER));
        assertEquals("time in some format", event.getParameters().get(EventDataKeys.TIMESTAMP));
    }

    @Test
    public void timestampIsOptionalForIncomingMessage() {
        SmsHttpTemplate template = new SmsHttpTemplate();
        SmsHttpTemplate.Incoming incoming = new SmsHttpTemplate.Incoming();
        incoming.setMessageKey("message");
        incoming.setSenderKey("sender");
        template.setIncoming(incoming);
        when(templateReader.getTemplate("/sms-http-template.json")).thenReturn(template);

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getParameter(template.getIncoming().getMessageKey())).thenReturn("some text message");
        when(httpServletRequest.getParameter(template.getIncoming().getSenderKey())).thenReturn("1234567890");

        new InboundSmsController(templateReader, eventRelay).handle(httpServletRequest);

        ArgumentCaptor<MotechEvent> eventCaptor = ArgumentCaptor.forClass(MotechEvent.class);
        verify(eventRelay).sendEventMessage(eventCaptor.capture());

        MotechEvent event = eventCaptor.getValue();
        assertEquals(null, event.getParameters().get(EventDataKeys.TIMESTAMP));
    }
}
