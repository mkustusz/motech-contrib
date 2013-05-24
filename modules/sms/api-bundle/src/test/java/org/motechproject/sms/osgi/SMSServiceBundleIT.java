package org.motechproject.sms.osgi;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventListener;
import org.motechproject.event.listener.EventListenerRegistry;
import org.motechproject.event.listener.EventListenerRegistryService;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.scheduler.MotechSchedulerService;
import org.motechproject.security.service.MotechUserService;
import org.motechproject.sms.api.constants.EventSubjects;
import org.motechproject.sms.api.service.SendSmsRequest;
import org.motechproject.sms.api.service.SmsService;
import org.motechproject.testing.osgi.BaseOsgiIT;
import org.motechproject.testing.utils.PollingHttpClient;
import org.motechproject.testing.utils.TestContext;
import org.motechproject.testing.utils.Wait;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

public class SMSServiceBundleIT extends BaseOsgiIT {

    private String HOST = "localhost";
    private int PORT = TestContext.getJettyPort();

    public void testSmsService() throws InterruptedException {

        final Object lock = new Object();
        final String subject = EventSubjects.SEND_SMS;
        final List<String> eventsReceived = new ArrayList<>();


        assertNotNull(bundleContext.getServiceReference(EventRelay.class.getName()));
        ServiceReference eventListenerRegistryRef = bundleContext.getServiceReference(EventListenerRegistryService.class.getName());
        assertNotNull(eventListenerRegistryRef);


        EventListenerRegistry eventListenerRegistry = (EventListenerRegistry) bundleContext.getService(eventListenerRegistryRef);


        eventListenerRegistry.registerListener(new EventListener() {
            @Override
            public void handle(MotechEvent event) {
                eventsReceived.add(event.getSubject());
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public String getIdentifier() {
                return subject;
            }
        }, subject);


        assertNotNull(bundleContext.getServiceReference(MotechSchedulerService.class.getName()));

        ServiceReference smsServiceReference = bundleContext.getServiceReference(SmsService.class.getName());
        assertNotNull(smsServiceReference);

        SmsService smsService = (SmsService) getApplicationContext().getBean("smsServiceRef");
        assertNotNull(smsService);


        smsService.sendSMS(new SendSmsRequest(asList("1234"), "Hi"));


        new Wait(lock, 2000).start();

        assertEquals(1, eventsReceived.size());
        assertTrue(eventsReceived.contains(subject));
    }

    public void testSmsWebServiceAsAnonymousUser() throws InterruptedException, IOException {
        PollingHttpClient motechHttpClient = new PollingHttpClient();
        String url = String.format("http://%s:%d%s", HOST, PORT, "/smsapi/web-api/messages");
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity("{\"recipients\" : [\"123\"], \"message\": \"foo\"}"));
        HttpResponse response = motechHttpClient.execute(request);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
    }

    public void testSmsWebServiceAsUnauthenticatedUser() throws InterruptedException, IOException {
        PollingHttpClient httpClient = new PollingHttpClient();
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("mal", "icious"));
        HttpPost request = new HttpPost(String.format("http://%s:%d%s", HOST, PORT, "/smsapi/web-api/messages"));
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity("{\"recipients\" : [\"123\"], \"message\": \"foo\"}"));
        HttpResponse response = httpClient.execute(request);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
    }

    public void testSmsWebServiceAsUnauthorizedUser() throws InterruptedException, IOException {
        ServiceReference motechUserServiceRef = bundleContext.getServiceReference(MotechUserService.class.getName());
        MotechUserService motechUserService = (MotechUserService) bundleContext.getService(motechUserServiceRef);
        motechUserService.register("fuu", "bar", "fuubar@a.com", "fuubar", asList("Admin User"), Locale.ENGLISH);

        PollingHttpClient httpClient = new PollingHttpClient();
        HttpPost request = new HttpPost(String.format("http://%s:%d%s", HOST, PORT, "/smsapi/web-api/messages"));
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Basic " + encodeBase64String("fuu:bar".getBytes("UTF-8")));
        request.setEntity(new StringEntity("{\"recipients\" : [\"123\"], \"message\": \"hello\"}"));
        HttpResponse response = httpClient.execute(request);
        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusLine().getStatusCode());
    }

    public void testSmsWebServiceAsAuthorizedUser() throws InterruptedException, IOException {
        ServiceReference motechUserServiceRef = bundleContext.getServiceReference(MotechUserService.class.getName());
        MotechUserService motechUserService = (MotechUserService) bundleContext.getService(motechUserServiceRef);
        motechUserService.register("foo", "bar", "foobar@a.com", "foobar", asList("Sms Admin"), Locale.ENGLISH);

        PollingHttpClient httpClient = new PollingHttpClient();
        HttpPost request = new HttpPost(String.format("http://%s:%d%s", HOST, PORT, "/smsapi/web-api/messages"));
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Authorization", "Basic " + encodeBase64String("foo:bar".getBytes("UTF-8")));
        request.setEntity(new StringEntity("{\"recipients\" : [\"123\"], \"message\": \"hello\"}"));
        HttpResponse response = httpClient.execute(request);
        assertEquals(HttpStatus.SC_CREATED, response.getStatusLine().getStatusCode());
    }

    @Override
    protected List<String> getImports() {
        return asList("org.motechproject.event");
    }


    @Override
    protected String[] getConfigLocations() {
        return new String[]{"/META-INF/spring/testSmsApiBundleContext.xml"};
    }

}