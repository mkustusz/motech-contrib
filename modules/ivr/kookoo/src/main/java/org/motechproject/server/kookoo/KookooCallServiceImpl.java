package org.motechproject.server.kookoo;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;
import org.motechproject.ivr.service.CallRequest;
import org.motechproject.ivr.service.IVRService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Logger;

@Service("ivrServiceKookoo")
public class KookooCallServiceImpl implements IVRService {
    public static final String OUTBOUND_URL = "kookoo.outbound.url";
    public static final String API_KEY = "kookoo.api.key";
    public static final String API_KEY_KEY = "api_key";
    public static final String URL_KEY = "url";
    public static final String CALLBACK_URL_KEY = "callback_url";
    public static final String PHONE_NUMBER_KEY = "phone_no";
    public static final String CUSTOM_DATA_KEY = "dataMap";
    public static final String IS_OUTBOUND_CALL = "is_outbound_call";

    private SettingsFacade settings;

    private HttpClient httpClient;
    private Logger log = Logger.getLogger(this.getClass().getName());

    @Autowired
    public KookooCallServiceImpl(SettingsFacade settings) {
        this(settings, new HttpClient());
    }

    KookooCallServiceImpl(SettingsFacade settings, HttpClient httpClient) {
        this.settings = settings;
        this.httpClient = httpClient;
    }

    @Override
    public void initiateCall(CallRequest callRequest) {
        if (callRequest == null) throw new IllegalArgumentException("Missing call request");

        try {
            final String externalId = callRequest.getPayload().get(EXTERNAL_ID);
            callRequest.getPayload().put(IS_OUTBOUND_CALL, "true");
            JSONObject json = new JSONObject(callRequest.getPayload());

            String applicationCallbackUrl = String.format(
                "%s/callback?%s=%s&%s=%s", callRequest.getCallBackUrl(), EXTERNAL_ID, externalId, CALL_TYPE, callRequest.getPayload().get(CALL_TYPE));
            String applicationReplyUrl = String.format(
                "%s?%s=%s", callRequest.getCallBackUrl(), CUSTOM_DATA_KEY, json.toString());

            GetMethod getMethod = new GetMethod(settings.getProperty(OUTBOUND_URL));
            getMethod.setQueryString(new NameValuePair[]{
                new NameValuePair(API_KEY_KEY, settings.getProperty(API_KEY)),
                new NameValuePair(URL_KEY, applicationReplyUrl),
                new NameValuePair(PHONE_NUMBER_KEY, callRequest.getPhone()),
                new NameValuePair(CALLBACK_URL_KEY, applicationCallbackUrl)
            });
            log.info(String.format("Dialing %s", getMethod.getURI()));
            httpClient.executeMethod(getMethod);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}