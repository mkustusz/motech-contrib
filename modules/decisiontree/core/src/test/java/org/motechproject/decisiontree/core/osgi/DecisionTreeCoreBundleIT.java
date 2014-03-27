package org.motechproject.decisiontree.core.osgi;

import org.motechproject.decisiontree.core.DecisionTreeService;
import org.motechproject.decisiontree.core.model.Tree;
import org.motechproject.testing.osgi.BaseOsgiIT;

import java.util.Arrays;
import java.util.List;

public class DecisionTreeCoreBundleIT extends BaseOsgiIT {

    public void testDecisionTreeService() {
        DecisionTreeService decisionTreeService = getService(DecisionTreeService.class);

        String treeName = "DecisionTreeCoreBundleIT";
        Tree tree = new Tree().setName(treeName);
        try {
            decisionTreeService.saveDecisionTree(tree);
            assertEquals(tree, decisionTreeService.getDecisionTree(tree.getId()));
        } finally {
            decisionTreeService.deleteDecisionTree(tree.getId());
        }
    }

    @Override
    protected List<String> getImports() {
        return Arrays.asList("org.motechproject.decisiontree.core.model");
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"testDecisionTreeContext.xml"};
    }
}
