package org.ei.commcare.api.contract;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class CommCareModuleDefinitions {
    private String userName;
    private String password;
    private List<CommCareModuleDefinition> modules;

    public List<CommCareFormDefinition> definitions() {
        ArrayList<CommCareFormDefinition> definitions = new ArrayList<CommCareFormDefinition>();
        for (CommCareModuleDefinition module : modules) {
            definitions.addAll(module.definitions());
        }
        return definitions;
    }

    public String userName() {
        return userName;
    }

    public String password() {
        return password;
    }

    public List<CommCareModuleDefinition> modules() {
        return modules;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
