package org.motechproject.ivr.web;

import com.google.gson.JsonParser;
import org.apache.http.impl.client.BasicResponseHandler;
import org.motechproject.callflow.service.CalllogSearchService;
import org.motechproject.testing.osgi.BaseOsgiIT;
import org.motechproject.testing.utils.PollingHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.osgi.framework.ServiceReference;

import java.io.IOException;

public class CalllogBundleIT extends BaseOsgiIT {

    public void testCalllogSearch() throws IOException, InterruptedException {
        final ServiceReference serviceReference = bundleContext.getServiceReference(CalllogSearchService.class.getName());
        assertNotNull(serviceReference);
        PollingHttpClient httpClient = new PollingHttpClient();
        String response = httpClient.get(String.format("http://localhost:%d/callLog/search", TestContext.getJettyPort()),
                new BasicResponseHandler());

        assertTrue(new JsonParser().parse(response).isJsonArray());
    }
}
