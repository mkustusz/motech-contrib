package org.ei.commcare.api.domain;

import org.ei.commcare.api.contract.CommCareFormDefinition;

import java.io.Serializable;
import java.util.Map;

public class CommCareFormInstance implements Serializable {
    private static final long serialVersionUID = 42L;

    private String formName;
    private String formId;
    private Map<String, String> fieldsWeCareAbout;

    public CommCareFormInstance(CommCareFormDefinition formDefinition, CommCareFormContent content) {
        this.formName = formDefinition.name();
        this.fieldsWeCareAbout = content.getValuesOfFieldsSpecifiedByPath(formDefinition.mappings());
        this.formId = content.formId();
    }

    public String formName() {
        return formName;
    }

    public String formId() {
        return formId;
    }

    public Map<String, String> fields() {
        return fieldsWeCareAbout;
    }
}
