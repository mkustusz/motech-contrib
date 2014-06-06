package org.motechproject.commcare.web;

import org.motechproject.commcare.domain.CommcareDataForwardingEndpoint;
import org.motechproject.commcare.domain.SettingsDto;
import org.motechproject.commcare.service.CommcareDataForwardingEndpointService;
import org.motechproject.server.config.SettingsFacade;
import org.motechproject.config.service.ConfigurationService;
import org.osgi.framework.BundleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Controller
public class SettingsController {

    private static final String COMMCARE_BASE_URL_KEY = "commcareBaseUrl";
    private static final String COMMCARE_DOMAIN_KEY = "commcareDomain";
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";

    private static final String CASE_EVENT_STRATEGY_KEY = "eventStrategy";

    private static final String FORWARD_CASES_KEY = "forwardCases";
    private static final String FORWARD_FORMS_KEY = "forwardForms";
    private static final String FORWARD_FORM_STUBS_KEY = "forwardFormStubs";
    private static final String FORWARD_SCHEMA_CHANGES_KEY = "forwardAppStructure";

    @Autowired
    private ConfigurationService settingsService;

    @Autowired
    private CommcareDataForwardingEndpointService forwardingEndpointService;

    private SettingsFacade settingsFacade;

    @Autowired
    public SettingsController(@Qualifier("commcareAPISettings") final SettingsFacade settingsFacade) {
        this.settingsFacade = settingsFacade;
    }

    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    @ResponseBody
    public SettingsDto getSettings() {
        SettingsDto dto = new SettingsDto();
        dto.setCommcareBaseUrl(getPropertyValue(COMMCARE_BASE_URL_KEY));
        dto.setCommcareDomain(getPropertyValue(COMMCARE_DOMAIN_KEY));
        dto.setUsername(getPropertyValue(USERNAME_KEY));
        dto.setPassword(getPropertyValue(PASSWORD_KEY));
        dto.setEventStrategy(getPropertyValue(CASE_EVENT_STRATEGY_KEY));
        dto.setForwardCases(getBooleanPropertyValue(FORWARD_CASES_KEY));
        dto.setForwardForms(getBooleanPropertyValue(FORWARD_FORMS_KEY));
        dto.setForwardFormStubs(getBooleanPropertyValue(FORWARD_FORM_STUBS_KEY));
        dto.setForwardAppStructure(getBooleanPropertyValue(FORWARD_SCHEMA_CHANGES_KEY));
        return dto;
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public void saveSettings(@RequestBody SettingsDto settings) throws BundleException {
        settingsFacade.setProperty(COMMCARE_BASE_URL_KEY, settings.getCommcareBaseUrl());
        settingsFacade.setProperty(COMMCARE_DOMAIN_KEY, settings.getCommcareDomain());
        settingsFacade.setProperty(USERNAME_KEY, settings.getUsername());
        settingsFacade.setProperty(PASSWORD_KEY, settings.getPassword());
        if (settings.getEventStrategy() != null) {
            settingsFacade.setProperty(CASE_EVENT_STRATEGY_KEY, settings.getEventStrategy());
        }

        if (!getBooleanPropertyValue(FORWARD_CASES_KEY) && settings.shouldForwardCases()) {
            settingsFacade.setProperty(FORWARD_CASES_KEY, String.valueOf(settings.shouldForwardCases()));

            forward("CaseRepeater", getCasesUrl());
        }

        if (!getBooleanPropertyValue(FORWARD_FORMS_KEY) && settings.shouldForwardForms()) {
            settingsFacade.setProperty(FORWARD_FORMS_KEY, String.valueOf(settings.shouldForwardForms()));

            forward("FormRepeater", getFormsUrl());
        }

        if (!getBooleanPropertyValue(FORWARD_FORM_STUBS_KEY) && settings.shouldForwardFormStubs()) {
            settingsFacade.setProperty(FORWARD_FORM_STUBS_KEY, String.valueOf(settings.shouldForwardFormStubs()));

            forward("ShortFormRepeater", getFormStubsUrl());
        }

        if (!getBooleanPropertyValue(FORWARD_SCHEMA_CHANGES_KEY) && settings.shouldForwardAppStructure()) {
            settingsFacade.setProperty(FORWARD_SCHEMA_CHANGES_KEY, String.valueOf(settings.shouldForwardAppStructure()));

            forward("AppStructureRepeater", getShemaChangeUrl());
        }
    }

    private String getPropertyValue(final String propertyKey) {
        String propertyValue = settingsFacade.getProperty(propertyKey);
        return isNotBlank(propertyValue) ? propertyValue : null;
    }

    private boolean getBooleanPropertyValue(final String propertyKey) {
        return Boolean.parseBoolean(settingsFacade.getProperty(propertyKey));
    }

    private void forward(String type, String url) {
        CommcareDataForwardingEndpoint newForwardingEndpoint = new CommcareDataForwardingEndpoint(
                getPropertyValue(COMMCARE_DOMAIN_KEY), type,
                url, null);

        forwardingEndpointService.createNewDataForwardingRule(newForwardingEndpoint);
    }

    private String getCasesUrl() {
        return settingsService.getPlatformSettings().getServerUrl() + "/module/commcare/cases/";
    }

    private String getFormsUrl() {
        return settingsService.getPlatformSettings().getServerUrl() + "/module/commcare/forms/";
    }

    private String getFormStubsUrl() {
        return settingsService.getPlatformSettings().getServerUrl() + "/module/commcare/stub/";
    }

    private String getShemaChangeUrl() {
        return settingsService.getPlatformSettings().getServerUrl() + "/module/commcare/appSchemaChange/";
    }
}
