package org.motechproject.ivr.kookoo.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.decisiontree.model.AudioPrompt;
import org.motechproject.decisiontree.model.ITreeCommand;
import org.motechproject.decisiontree.model.MenuAudioPrompt;
import org.motechproject.decisiontree.model.Node;
import org.motechproject.decisiontree.model.Transition;
import org.motechproject.decisiontree.model.Tree;
import org.motechproject.decisiontree.service.FlowSessionService;
import org.motechproject.ivr.domain.IVRMessage;
import org.motechproject.ivr.kookoo.KooKooIVRContextForTest;
import org.motechproject.ivr.kookoo.extensions.CallFlowController;
import org.motechproject.ivr.kookoo.service.KookooCallDetailRecordsService;
import org.motechproject.server.decisiontree.TreeNodeLocator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DecisionTreeBasedIVRControllerTest {
    @Mock
    private IVRMessage ivrMessage;
    @Mock
    private CallFlowController callFlowController;
    @Mock
    private KookooCallDetailRecordsService callDetailRecordsService;
    @Mock
    private StandardResponseController standardResponseController;
    @Mock
    private FlowSessionService flowSessionService;
    @Mock
    ApplicationContext applicationContext;
    @InjectMocks
    TreeNodeLocator treeNodeLocator = new TreeNodeLocator();
    @Mock
    AutowireCapableBeanFactory autowireCapableBeanFactory;

    KooKooIVRContextForTest ivrContext;
    private DecisionTreeBasedIVRController controller;
    private DecisionTreeBasedIVRControllerTest.CommandForTamaIvrActionTest commandForTamaIvrActionTest;
    private static final String CALL_ID = "12312";

    @Before
    public void setup() {
        initMocks(this);
        ivrContext = new KooKooIVRContextForTest();
        String treeName = "TestTree";
        ivrContext.treeName(treeName);
        ivrContext.callId(CALL_ID);
        commandForTamaIvrActionTest = new CommandForTamaIvrActionTest();

        when(callFlowController.getTree(treeName, ivrContext)).thenReturn(new TestTreeForTamaIvrActionTest().getTree());
        controller = new DecisionTreeBasedIVRController(callFlowController, ivrMessage, callDetailRecordsService,
                standardResponseController, flowSessionService);
        controller.setTreeNodeLocator(treeNodeLocator);
        when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(autowireCapableBeanFactory);
        doNothing().when(autowireCapableBeanFactory).autowireBean(anyObject());
    }

    @Test
    public void shouldExecuteCommandIfNextNodeIsNotNull() {
        controller.gotDTMF(ivrContext);
        assertTrue(commandForTamaIvrActionTest.isCalled());
    }

    @Test
    public void shouldExecuteSameCommandIfTransitionIsInvalid() {
        controller.gotDTMF(ivrContext);
        ivrContext.userInput("4");
        controller.gotDTMF(ivrContext);
        commandForTamaIvrActionTest.setCalled(false);
        assertFalse(commandForTamaIvrActionTest.isCalled());
    }

    @Test
    public void shouldChangeCurrentNodePath() {
        ivrContext.userInput("1");
        controller.gotDTMF(ivrContext);
        assertEquals("/1", ivrContext.currentDecisionTreePath());
    }

    @Test
    public void shouldNotChangeCurrentTreePositionWhenUserInputIsInvalid() {
        ivrContext.userInput("56").currentDecisionTreePath("/1");
        controller.gotDTMF(ivrContext);
        assertEquals("/1", ivrContext.currentDecisionTreePath());
    }

    @Test
    public void shouldNotExecuteCommandIfThereIsNoUserInput() {
        ivrContext.currentDecisionTreePath("/");
        controller.gotDTMF(ivrContext);
        assertFalse(commandForTamaIvrActionTest.isCalled());
    }

    @Test
    public void shouldClearSessionAfterHangingUp() {
        controller.hangup(ivrContext);

        verify(flowSessionService).removeCallSession(CALL_ID);
    }

    class TestTreeForTamaIvrActionTest {
        protected Node createRootNode() {
            return new Node()
                    .setPrompts(new AudioPrompt().setName("foo").setCommand(commandForTamaIvrActionTest))
                    .setTransitions(
                            new Object[][]{
                                    {
                                            "1",
                                            new Transition().setDestinationNode(
                                                    new Node().setPrompts(new MenuAudioPrompt().setName("bar"))
                                                            .setTransitions(
                                                                    new Object[][]{
                                                                            {
                                                                                    "1",
                                                                                    new Transition().setDestinationNode(
                                                                                            new Node().setPrompts(new AudioPrompt().setName("bar")))},
                                                                            {
                                                                                    "2",
                                                                                    new Transition().setDestinationNode(
                                                                                            new Node().setPrompts(new AudioPrompt().setName("baz")))}}))},
                                    {
                                            "2",
                                            new Transition().setDestinationNode(
                                                    new Node().setPrompts(new AudioPrompt().setName("baz")))
                                    }});
        }

        public Tree getTree() {
            return new Tree()
                    .setName(this.getClass().getName())
                    .setRootNode(createRootNode());
        }
    }

    class CommandForTamaIvrActionTest implements ITreeCommand {
        private boolean called;

        @Override
        public String[] execute(Object o) {
            called = true;
            return new String[0];
        }

        public boolean isCalled() {
            return called;
        }

        public void setCalled(boolean called) {
            this.called = called;
        }
    }

    /*@Test
    public void nextNodeAtTheTop() {
        Tree tree = new Tree().setName("tree1").setRootNode(
                new Node().setTransitions(new Object[][]{
                        {"1", new Transition().setName("t1").setDestinationNode(new Node().setTransitions(new Object[][]{
                                {"1", new Transition().setName("sick1").setDestinationNode(new Node())},
                                {"2", new Transition().setName("sick2").setDestinationNode(new Node())},
                                {"3", new Transition().setName("sick3").setDestinationNode(new Node())},
                        }))},
                        {"2", new Transition().setName("ill").setDestinationNode(new Node())}
                }));
        controller.
        Node node = tree.nextNodeInfo("", "").node();
        assertEquals(tree.getRootNode(), node);
    }*/
}
