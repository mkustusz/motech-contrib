package org.motechproject.server.decisiontree.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.motechproject.decisiontree.model.AudioPrompt;
import org.motechproject.decisiontree.model.Node;
import org.motechproject.decisiontree.model.Transition;
import org.motechproject.decisiontree.model.Tree;
import org.motechproject.server.decisiontree.TreeNodeLocator;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class TreeNodeLocatorTest {

    Tree tree;

    @InjectMocks
    TreeNodeLocator locator = new TreeNodeLocator();

    @Mock
    ApplicationContext applicationContext;
    @Mock
    AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(applicationContext.getAutowireCapableBeanFactory()).thenReturn(autowireCapableBeanFactory);
        doNothing().when(autowireCapableBeanFactory).autowireBean(any());
        tree = new Tree().setName("tree1").setRootNode(
                new Node().setTransitions(new Object[][]{
                        {"1", new Transition().setName("t1").setDestinationNode(new Node().setTransitions(new Object[][]{
                                {"1", new Transition().setName("sick1").setDestinationNode(new Node())},
                                {"2", new Transition().setName("sick2").setDestinationNode(new Node())},
                                {"3", new Transition().setName("sick3").setDestinationNode(new Node())},
                        }))},
                        {"2", new Transition().setName("ill").setDestinationNode(new Node())}
                }));

    }

    @Test
    public void testFindNode() {
        assertNotNull(locator.findNode(tree, "/", null));
        assertNotNull(locator.findNode(tree, "/1/2", null));
        assertNotNull(locator.findNode(tree, "/1/2/", null));
        assertNotNull(locator.findNode(tree, "//1/2", null));
        assertNotNull(locator.findNode(tree, "//1/2/", null));
        assertNull(locator.findNode(tree, "/2/1/2/", null));
        assertNull(locator.findNode(tree, "3", null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTreeNull() {
        locator.findNode(null, "", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathNull() {
        locator.findNode(tree, null, null);
    }

    @Test
    public void addPrompts() {
        final Node rootNode = tree.getRootNode();
        rootNode.setPrompts(new AudioPrompt());
        rootNode.addPrompts(new AudioPrompt());

        assertEquals(2, rootNode.getPrompts().size());
    }

    @Test
    public void addPromptToBeginning() {
        final Node rootNode = tree.getRootNode();
        AudioPrompt audioPrompt_1 = new AudioPrompt();
        audioPrompt_1.setName("1");
        AudioPrompt audioPrompt_2 = new AudioPrompt();
        audioPrompt_2.setName("2");

        rootNode.addPrompts(audioPrompt_1);
        assertEquals(1, rootNode.getPrompts().size());

        rootNode.addPromptToBeginning(audioPrompt_2);
        assertEquals(2, rootNode.getPrompts().size());
        assertEquals("2", rootNode.getPrompts().get(0).getName());
        assertEquals("1", rootNode.getPrompts().get(1).getName());
    }
}
