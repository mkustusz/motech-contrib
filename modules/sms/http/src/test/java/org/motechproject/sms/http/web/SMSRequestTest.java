package org.motechproject.sms.http.web;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.motechproject.sms.http.domain.SMSRequest;

import static org.junit.Assert.assertThat;

public class SMSRequestTest {

    @Test
    public void shouldValidateRecipientNumber(){
       assertThat(new SMSRequest("Message","1234").isValid(), Is.is(true));
       assertThat(new SMSRequest("Message",null).isValid(), Is.is(false));
       assertThat(new SMSRequest("Message","").isValid(), Is.is(false));
       assertThat(new SMSRequest(null,"123").isValid(), Is.is(false));
       assertThat(new SMSRequest("","123").isValid(), Is.is(false));
    }



}
