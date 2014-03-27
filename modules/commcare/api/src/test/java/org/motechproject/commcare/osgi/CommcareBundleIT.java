package org.motechproject.commcare.osgi;

import com.google.gson.JsonParser;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.motechproject.commcare.service.CommcareCaseService;
import org.motechproject.commcare.service.CommcareFormService;
import org.motechproject.commcare.service.CommcareUserService;
import org.motechproject.testing.osgi.BaseOsgiIT;
import org.motechproject.testing.utils.PollingHttpClient;
import org.motechproject.testing.utils.TestContext;

import java.io.IOException;

public class CommcareBundleIT extends BaseOsgiIT {

    public void testServices() {
        getService(CommcareUserService.class);
        getService(CommcareCaseService.class);
        getService(CommcareFormService.class);
    }

    public void testSettingsController() throws IOException, InterruptedException {
        final String response = new PollingHttpClient(new DefaultHttpClient(), 60)
                .get(String.format("http://localhost:%d/commcare/settings", TestContext.getJettyPort()), new BasicResponseHandler());
        assertNotNull(response);
        assertTrue(new JsonParser().parse(response).isJsonObject());
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"/META-INF/spring/testCommcareBundleContext.xml"};
    }
}
