package org.motechproject.decisiontree.osgi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.decisiontree.DecisionTreeService;
import org.motechproject.decisiontree.model.Tree;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class DecisionTreeCoreBundleIT extends BasePaxIT {

    @Inject
    private DecisionTreeService decisionTreeService;

    @Test
    public void testDecisionTreeService() {
        String treeName = "DecisionTreeCoreBundleIT";
        Tree tree = new Tree().setName(treeName);
        try {
            decisionTreeService.saveDecisionTree(tree);
            assertEquals(tree, decisionTreeService.getDecisionTree(tree.getId()));
        } finally {
            decisionTreeService.deleteDecisionTree(tree.getId());
        }
    }
}
