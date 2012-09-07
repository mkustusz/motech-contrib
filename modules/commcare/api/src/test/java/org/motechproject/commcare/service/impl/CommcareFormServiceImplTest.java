package org.motechproject.commcare.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.motechproject.commcare.domain.CaseInfo;
import org.motechproject.commcare.domain.CommcareForm;
import org.motechproject.commcare.domain.FormValueElement;
import org.motechproject.commcare.service.impl.CommcareCaseServiceImpl;
import org.motechproject.commcare.util.CommCareAPIHttpClient;

import com.google.common.collect.Multimap;

public class CommcareFormServiceImplTest {

    private CommcareFormServiceImpl formService;

    @Mock
    private CommCareAPIHttpClient commcareHttpClient;

    @Before
    public void setUp() {
        initMocks(this);
        formService = new CommcareFormServiceImpl(commcareHttpClient);
    }

    @Test
    public void testAllCases() {
        when(commcareHttpClient.formRequest(Matchers.anyString())).thenReturn(jsonRegistrationForm());

        CommcareForm form = formService.retrieveForm("testForm1");

        assertEquals(form.getId(), "4432638b-8f77-42f5-8c65-9fc18b26fb09");
        assertEquals(form.getMd5(), "75cb64ec8ead19684895e0822a960149");
        assertEquals(form.getResourceUri(), "");
        assertEquals(form.getType(), "data");
        assertEquals(form.getUiversion(), "1");
        assertEquals(form.getVersion(), "1");

        Map<String, String> metaData = form.getMetadata();

        assertNotNull(metaData);
        assertEquals(metaData.size(), 9);

        assertEquals(metaData.get("@xmlns"), "http://openrosa.org/jr/xforms");
        assertEquals(
                metaData.get("appVersion"),
                "@xmlns:http://commcarehq.org/xforms, #text:CommCare ODK, version \"2.0\"(1090). CommCare Version 2.0. Build #1090, built on: May-23-2012");
        assertEquals(metaData.get("deviceID"), "A000002A46308E");
        assertEquals(metaData.get("instanceID"), "4432638b-8f77-42f5-8c65-9fc18b26fb09");
        assertEquals(metaData.get("timeEnd"), "2012-06-22T14:26:54");
        assertEquals(metaData.get("timeStart"), "2012-06-22T14:26:01");
        assertEquals(metaData.get("userID"), "abc707434d4ec780967fa65b7167cc58");
        assertEquals(metaData.get("username"), "test");
        assertNull(metaData.get("deprecatedID"));

        FormValueElement rootElement = form.getForm();

        Map<String, String> topLevelAttributes = rootElement.getAttributes();

        assertEquals(topLevelAttributes.size(), 4);
        assertEquals(rootElement.getValue(), "data");

        assertNull(rootElement.getElementByName("case"));
        assertNull(rootElement.getElementByName("create"));
        assertNotNull(rootElement.getElementByNameIncludeCase("case"));
        assertNotNull(rootElement.getElementByNameIncludeCase("create"));

        List<FormValueElement> elements = rootElement.getElementsByAttribute("concept_id", "5596");
        assertEquals(elements.size(), 1);

        assertEquals("form", rootElement.getElementName());

        Multimap<String, FormValueElement> subElements = rootElement.getSubElements();
        assertNotNull(subElements.get("case"));
        assertEquals(subElements.size(), 15);

        Collection<FormValueElement> childElement = subElements.get("family_planning_acceptance");
        FormValueElement firstElement = childElement.iterator().next();
        assertNotNull(firstElement);
        assertEquals(firstElement.getSubElements().size(), 3);
    }

    private String jsonRegistrationForm() {
        return "{\"form\":{\"#type\":\"data\",\"@name\":\"Register Pregnancy\",\"@uiVersion\":\"1\",\"@version\":\"1\",\"@xmlns\":\"http://openrosa.org/formdesigner/882FC273-E436-4BA1-B8CC-9CA526FFF8C2\",\"case\":{\"@case_id\":\"6bc2f8f6-b1da-4be2-98d4-1cb2d557a329\",\"@date_modified\":\"2012-06-22T14:26:54\",\"@user_id\":\"abc707434d4ec780967fa65b7167cc58\",\"@xmlns\":\"http://commcarehq.org/case/transaction/v2\",\"create\":{\"case_name\":\"Russell Gillen\",\"case_type\":\"pregnancy\",\"owner_id\":\"abc707434d4ec780967fa65b7167cc58\"},\"update\":{\"dob\":\"1996-06-22\",\"dob_calc\":\"1996-06-22\",\"dob_known\":\"yes\",\"edd_calc\":\"2012-12-23\",\"edd_known\":\"no\",\"external_id\":\"Reee\",\"first_name\":\"Russell\",\"full_name\":\"Russell Gillen\",\"health_id\":\"Reee\",\"household_head_health_id\":\"Fee\",\"mobile_phone_number\":\"2525252222\",\"pregnancy_month\":\"3\",\"surname\":\"Gillen\"}},\"dob\":\"1996-06-22\",\"dob_calc\":\"1996-06-22\",\"dob_known\":\"yes\",\"family_planning_acceptance\":{\"new_acceptors\":\"2\",\"repeat_acceptors\":{\"sublevel1\":{\"sublevel2\":[{\"#text\":\"4\",\"@conceptId\":\"4321\",\"@field\":\"blah\",\"@field2\":\"blah2\"},{\"#text\":\"3\",\"@conceptId\":\"1234\"}]}},\"total_acceptors\":\"2\"},\"edd_calc\":{\"#text\":\"2012-12-23\",\"@concept_id\":\"5596\"},\"edd_known\":\"no\",\"first_name\":\"Russell\",\"full_name\":\"Russell Gillen\",\"health_id\":\"Reee\",\"household_head_health_id\":\"Fee\",\"meta\":{\"@xmlns\":\"http://openrosa.org/jr/xforms\",\"appVersion\":{\"#text\":\"CommCare ODK, version \\\"2.0\\\"(1090). CommCare Version 2.0. Build #1090, built on: May-23-2012\",\"@xmlns\":\"http://commcarehq.org/xforms\"},\"deviceID\":\"A000002A46308E\",\"instanceID\":\"4432638b-8f77-42f5-8c65-9fc18b26fb09\",\"timeEnd\":\"2012-06-22T14:26:54\",\"timeStart\":\"2012-06-22T14:26:01\",\"userID\":\"abc707434d4ec780967fa65b7167cc58\",\"username\":\"test\"},\"mobile_phone_number\":{\"#text\":\"2525252222\",\"@concept_id\":\"159635\"},\"pregnancy_month\":\"3\",\"surname\":\"Gillen\"},\"id\":\"4432638b-8f77-42f5-8c65-9fc18b26fb09\",\"md5\":\"75cb64ec8ead19684895e0822a960149\",\"metadata\":{\"@xmlns\":\"http://openrosa.org/jr/xforms\",\"appVersion\":\"@xmlns:http://commcarehq.org/xforms, #text:CommCare ODK, version \\\"2.0\\\"(1090). CommCare Version 2.0. Build #1090, built on: May-23-2012\",\"deprecatedID\":null,\"deviceID\":\"A000002A46308E\",\"instanceID\":\"4432638b-8f77-42f5-8c65-9fc18b26fb09\",\"timeEnd\":\"2012-06-22T14:26:54\",\"timeStart\":\"2012-06-22T14:26:01\",\"userID\":\"abc707434d4ec780967fa65b7167cc58\",\"username\":\"test\"},\"resource_uri\":\"\",\"type\":\"data\",\"uiversion\":\"1\",\"version\":\"1\"}";
    }
}
