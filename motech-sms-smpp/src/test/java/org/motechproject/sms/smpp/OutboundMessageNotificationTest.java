package org.motechproject.sms.smpp;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.scheduler.domain.MotechEvent;
import org.motechproject.scheduler.gateway.OutboundEventGateway;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.sms.api.DeliveryStatus;
import org.motechproject.sms.smpp.constants.SmsProperties;
import org.motechproject.sms.smpp.repository.AllOutboundSMS;
import org.smslib.AGateway;
import org.smslib.OutboundMessage;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.sms.api.constants.EventDataKeys.MESSAGE;
import static org.motechproject.sms.smpp.constants.EventDataKeys.RECIPIENT;

public class OutboundMessageNotificationTest {
    @Mock
    private AGateway gateway;
    @Mock
    private OutboundEventGateway outboundEventGateway;

    private OutboundMessageNotification outboundMessageNotification;
    @Mock
    private AllOutboundSMS mockAllOutboundSMS;

    @Before
    public void setUp() {
        initMocks(this);

        Properties smsProperties = new Properties() {{
            setProperty(SmsProperties.MAX_RETRIES, "4");
        }};
        SettingsFacade settings = new SettingsFacade();
        settings.addConfigProperties("sms.properties", smsProperties);

        outboundMessageNotification = new OutboundMessageNotification(outboundEventGateway, settings);
        ReflectionTestUtils.setField(outboundMessageNotification, "allOutboundSMS", mockAllOutboundSMS);
    }

    @Test
    public void shouldRaiseAnEventIfMessageDispatchHasFailedAfterMaxNumberOfRetries() {
        final String recipient = "9876543210";
        String myText = "Test Message";
        OutboundMessage message = new OutboundMessage(recipient, myText) {{
            setRefNo("refNo11111");
            setMessageStatus(OutboundMessage.MessageStatuses.FAILED);
            setRetryCount(4);
        }};


        outboundMessageNotification.process(gateway, message);

        ArgumentCaptor<MotechEvent> motechEventArgumentCaptor = ArgumentCaptor.forClass(MotechEvent.class);
        verify(outboundEventGateway).sendEventMessage(motechEventArgumentCaptor.capture());
        Map<String, Object> parameters = motechEventArgumentCaptor.getValue().getParameters();
        assertEquals(recipient, parameters.get(RECIPIENT));
        assertEquals("Test Message", parameters.get(MESSAGE));

        assertAuditMessage(recipient, "refNo11111", DeliveryStatus.ABORTED);
    }

    @Test
    public void shouldNotRaiseAnEventIfMessageDispatchHasFailedAndIsGoingToBeRetried() {
        String recipient = "9876543210";
        String myText = "Test Message";
        OutboundMessage message = new OutboundMessage(recipient, myText) {{
            setRefNo("refNo2222");
            setMessageStatus(OutboundMessage.MessageStatuses.FAILED);
            setRetryCount(1);
        }};

        outboundMessageNotification.process(gateway, message);

        verifyZeroInteractions(outboundEventGateway);
        assertAuditMessage(recipient, "refNo2222", DeliveryStatus.KEEPTRYING);
    }

    @Test
    public void shouldNotRaiseAnyEventIfMessageDispatchIsSuccessful() {
        String recipient = "9876543210";
        String myText = "Test Message";
        OutboundMessage message = new OutboundMessage(recipient, myText) {{
            setMessageStatus(OutboundMessage.MessageStatuses.SENT);
            setRefNo("refNo");
        }};

        outboundMessageNotification.process(gateway, message);

        verifyZeroInteractions(outboundEventGateway);
        assertAuditMessage(recipient, "refNo", DeliveryStatus.INPROGRESS);
    }

    private void assertAuditMessage(String recipient, String refNo, DeliveryStatus deliveryStatus) {
        ArgumentCaptor<OutboundSMS> outboundSMSCaptor = ArgumentCaptor.forClass(OutboundSMS.class);

        verify(mockAllOutboundSMS).createOrReplace(outboundSMSCaptor.capture());
        assertEquals(refNo, outboundSMSCaptor.getValue().getRefNo());
        assertEquals(deliveryStatus, outboundSMSCaptor.getValue().getDeliveryStatus());
        assertEquals(recipient, outboundSMSCaptor.getValue().getPhoneNumber());
    }
}
