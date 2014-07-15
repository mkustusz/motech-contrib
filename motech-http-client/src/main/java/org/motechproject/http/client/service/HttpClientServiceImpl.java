package org.motechproject.http.client.service;


import org.motechproject.event.MotechEvent;
import org.motechproject.http.client.components.CommunicationType;
import org.motechproject.http.client.domain.EventDataKeys;
import org.motechproject.http.client.domain.EventSubjects;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
public class HttpClientServiceImpl implements HttpClientService {

    @Resource(name = "${communication.type}")
    private CommunicationType communicationType;

    @Override
    public void post(String url, Object data, String username, String password) {
        HashMap<String, Object> parameters = constructParametersFrom(url, data, "POST", username, password);
        MotechEvent motechEvent = new MotechEvent(EventSubjects.HTTP_REQUEST, parameters);
        communicationType.send(motechEvent);
    }

    @Override
    public void put(String url, Object data, String username, String password) {
        HashMap<String, Object> parameters = constructParametersFrom(url, data, "PUT", username, password);
        MotechEvent motechEvent = new MotechEvent(EventSubjects.HTTP_REQUEST, parameters);
        communicationType.send(motechEvent);
    }

    private HashMap<String, Object> constructParametersFrom(String url, Object data, String method, String username, String password) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(EventDataKeys.URL, url);
        parameters.put(EventDataKeys.METHOD, method);
        parameters.put(EventDataKeys.DATA, data);
        parameters.put(EventDataKeys.USERNAME, username);
        parameters.put(EventDataKeys.PASSWORD, password);
        return parameters;
    }
}
