package org.motechproject.server.kookoo.it;

import org.ektorp.CouchDbConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.callflow.repository.AllFlowSessionRecords;
import org.motechproject.callflow.service.FlowSessionService;
import org.motechproject.decisiontree.core.model.Node;
import org.motechproject.decisiontree.core.model.TextToSpeechPrompt;
import org.motechproject.decisiontree.core.model.Transition;
import org.motechproject.decisiontree.core.model.Tree;
import org.motechproject.decisiontree.core.repository.AllTrees;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventListener;
import org.motechproject.event.listener.EventListenerRegistryService;
import org.motechproject.server.kookoo.web.KookooIvrController;
import org.motechproject.testing.utils.SpringIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static junit.framework.Assert.fail;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:META-INF/motech/*.xml"})
public class KookooIvrControllerEndOfCallIT extends SpringIntegrationTest {

    public static final int EVENT_TIMEOUT = 12000;
    public static final String END_OF_CALL_EVENT = "ivr.end_of_call.*";

    @Autowired
    @Qualifier("treesDatabase")
    CouchDbConnector connector;

    @Autowired
    AllTrees allTrees;

    @Autowired
    AllFlowSessionRecords allFlowSessionRecords;

    @Autowired
    KookooIvrController kookooIvrController;

    @Autowired
    private FlowSessionService flowSessionService;

    @Autowired
    EventListenerRegistryService eventListenerRegistry;

    private MockMvc mockKookooIvrController;

    @Before
    public void setup() throws Exception {
        mockKookooIvrController = MockMvcBuilders.standaloneSetup(kookooIvrController).build();
    }

    @Test
    public void shouldReceiveEndOfCallEventOnDisconnect() throws Exception {
        try {
            TestListener listener = new TestListener("end_of_call_disconnect_test_listener");
            eventListenerRegistry.registerListener(listener, END_OF_CALL_EVENT);

            Tree tree = new Tree();
            tree.setName("someTree");
            tree.setRootTransition(new Transition().setDestinationNode(
                    new Node()
                            .setPrompts(new TextToSpeechPrompt().setMessage("foo"))
            ));
            allTrees.addOrReplace(tree);

            mockKookooIvrController.perform(get("/web-api/ivr?tree=someTree&ln=en&event=Disconnect&data=31415&sid=123a"));

            Object lock = listener.getLock();
            synchronized (lock) {
                lock.wait(EVENT_TIMEOUT);
                if (!listener.isEventReceived()) {
                    fail(format("%s event not raised.", END_OF_CALL_EVENT));
                }
            }
        } finally {
            eventListenerRegistry.clearListenersForBean("end_of_call_test_disconnect_listener");
        }
    }

    @Test
    public void shouldReceiveEndOfCallEventOnHangup() throws Exception {
        try {
            TestListener listener = new TestListener("end_of_call_test_hangup_listener");
            eventListenerRegistry.registerListener(listener, END_OF_CALL_EVENT);

            Tree tree = new Tree();
            tree.setName("someTree");
            tree.setRootTransition(new Transition().setDestinationNode(
                    new Node()
                            .setPrompts(new TextToSpeechPrompt().setMessage("foo"))
            ));
            allTrees.addOrReplace(tree);

            mockKookooIvrController.perform(get("/web-api/ivr?tree=someTree&ln=en&event=Hangup&data=31415&sid=123a"));

            Object lock = listener.getLock();
            synchronized (lock) {
                lock.wait(EVENT_TIMEOUT);
                if (!listener.isEventReceived()) {
                    fail(format("%s event not raised.", END_OF_CALL_EVENT));
                }
            }
        } finally {
            eventListenerRegistry.clearListenersForBean("end_of_call_test_hangup_listener");
        }
    }


    @Test
    public void shouldReceiveEndOfCallEventOnMissedCall() throws Exception {
        try {
            TestListener listener = new TestListener("end_of_call_test_missed_call_listener");
            eventListenerRegistry.registerListener(listener, END_OF_CALL_EVENT);

            String sid = "mcid123";
            String phoneNumber = "12345";
            flowSessionService.findOrCreate(sid, phoneNumber);
            String url = String.format("/web-api/ivr/callstatus?status=ring&status_details=NoAnswer&sid=%s&phone_no=%s", sid, phoneNumber);
            mockKookooIvrController.perform(post(url));

            Object lock = listener.getLock();
            while (!listener.isEventReceived()) {
                synchronized (lock) {
                    lock.wait(EVENT_TIMEOUT);
                    if (!listener.isEventReceived()) {
                        fail(format("%s event not raised.", END_OF_CALL_EVENT));
                    }
                }
            }
        } finally {
            eventListenerRegistry.clearListenersForBean("end_of_call_test_hangup_listener");
        }
    }

    public static class TestListener implements EventListener {

        List<MotechEvent> eventsReceived = new ArrayList<>();
        private String id;

        public TestListener(String id) {
            this.id = id;
        }

        public Object getLock() {
            return eventsReceived;
        }

        public boolean isEventReceived() {
            return eventsReceived.size() > 0;
        }

        @Override
        public void handle(MotechEvent event) {
            synchronized (eventsReceived) {
                eventsReceived.add(event);
                eventsReceived.notifyAll();
            }
        }

        @Override
        public String getIdentifier() {
            return id;
        }
    }

    @Override
    public CouchDbConnector getDBConnector() {
        return connector;
    }

    @After
    public void teardown() {
        allTrees.removeAll();
        allFlowSessionRecords.removeAll();
    }
}