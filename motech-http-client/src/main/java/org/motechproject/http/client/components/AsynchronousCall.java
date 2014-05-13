package org.motechproject.http.client.components;

import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.MotechEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("asynchronous")
public class AsynchronousCall implements CommunicationType {

    private EventRelay eventRelay;

    @Autowired
    public AsynchronousCall(EventRelay eventRelay) {
        this.eventRelay = eventRelay;
    }

    public void send(MotechEvent motechEvent) {
        eventRelay.sendEventMessage(motechEvent);
    }
}
