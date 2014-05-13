package org.ei.commcare.api.util;

import org.ei.commcare.api.contract.CommCareModuleDefinitions;
import org.motechproject.commons.api.json.MotechJsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class CommCareImportProperties {
    public static final String COMMCARE_IMPORT_DEFINITION_FILE = "commcare-import.definition.file";
    private final Properties properties;

    @Autowired
    public CommCareImportProperties(@Qualifier("propertiesForCommCareImport") Properties properties) {
        this.properties = properties;
    }

    public CommCareModuleDefinitions moduleDefinitions() {
        String jsonPath = properties.getProperty(COMMCARE_IMPORT_DEFINITION_FILE);
        return (CommCareModuleDefinitions) new MotechJsonReader().readFromFile(jsonPath, CommCareModuleDefinitions.class);
    }
}
