package org.motechproject.sms.api.web;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.motechproject.sms.api.SMSRequest;
import org.motechproject.sms.api.SmsDeliveryFailureException;
import org.motechproject.sms.api.service.SendSmsRequest;
import org.motechproject.sms.api.service.SmsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SMSControllerTest {


    @Mock
    private SmsService smsService;
    private SMSController smsController;


    @Before
    public void setUp() throws Exception {
        smsController = new SMSController(smsService);
    }

    @Test
    public void shouldSendSMS() throws SmsDeliveryFailureException {

        String message = "Hello World";
        String recipient = "9886991167";
        ResponseEntity<String> responseEntity = smsController.send(new SMSRequest(message, recipient));

        assertThat(HttpStatus.OK, Is.is(responseEntity.getStatusCode()));

        verify(smsService).sendSMS(new SendSmsRequest(asList(recipient), message));

    }


    @Test
    public void shouldReturnInvalidRequestIfPhoneNumberInvalid() throws SmsDeliveryFailureException {
        ResponseEntity<String> responseEntity = smsController.send(new SMSRequest("message", ""));
        assertThat(responseEntity.getStatusCode(), Is.is(HttpStatus.BAD_REQUEST));
    }


}
