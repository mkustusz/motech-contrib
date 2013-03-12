package org.motechproject.server.verboice;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.motechproject.ivr.service.IVRService;
import org.motechproject.testing.osgi.BaseOsgiIT;

import java.io.IOException;

public class VerboiceBundleIT extends BaseOsgiIT {

    public void testThatVerboiceIvrServicesIsAvailableOnImport() {
        IVRService ivrService = (IVRService) getApplicationContext().getBean("testIvrServiceOsgi");
        assertNotNull(ivrService);
    }


    public void testThatVerboiceUrlIsAccessible() throws IOException, InterruptedException {
        HttpResponse response = executeHttpCall("localhost", 8080, "/verboice/ivr?CallStatus=no-answer&CallSid=123A&From=12345", new ResponseHandler<HttpResponse>() {
            @Override
            public HttpResponse handleResponse(HttpResponse response) throws IOException {
                return response;
            }
        });

        assertNotNull(response);
        assertEquals(response.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
    }


    @Override
    protected String[] getConfigLocations() {
        return new String[]{"/META-INF/osgi/testIvrVerboiceOsgiContext.xml"};
    }

}
