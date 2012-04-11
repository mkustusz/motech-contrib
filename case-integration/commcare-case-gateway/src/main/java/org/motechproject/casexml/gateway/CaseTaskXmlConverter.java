package org.motechproject.casexml.gateway;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.motechproject.casexml.domain.CaseTask;
import org.motechproject.casexml.request.*;
import org.motechproject.casexml.request.converter.PregnancyConverter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CaseTaskXmlConverter {

    public CaseTaskXmlConverter() {
    }

    public String convertToCaseXmlWithEnvelope(CaseTask task) {
        CaseRequest caseRequest = mapToCase(task);
        CommcareRequestData request = createRequestWithEnvelope(caseRequest);

        return convertToXml(request);
    }

    private CaseRequest mapToCase(CaseTask task) {
        CaseRequest ccCase = createCase(task);

        CreateElement create = new CreateElement(task.getCaseType(), task.getCaseName(), task.getOwnerId());
        ccCase.setCreateElement(create);
        UpdateElement update = new UpdateElement(task.getTaskId(), task.getDateEligible(), task.getDateExpires());
        ccCase.setUpdateElement(update);

        Index index = new Index(new Pregnancy(task.getClientCaseId(), task.getClientCaseType()));
        ccCase.setIndex(index);

        return ccCase;
    }

    private CaseRequest createCase(CaseTask task) {
        return new CaseRequest(task.getCaseId(),task.getMotechUserId(),task.getCurrentTime());
    }

    private String convertToXml(CommcareRequestData request) {

        XStream xstream = new XStream(new DomDriver("UTF-8", new NoNameCoder()));

        xstream.alias("data", CommcareRequestData.class);
        xstream.useAttributeFor(CommcareRequestData.class, "xmlns");

        xstream.alias("meta", MetaElement.class);
        xstream.useAttributeFor(MetaElement.class, "xmlns");

        xstream.aliasField("case", CommcareRequestData.class, "ccCase");
        xstream.aliasField("create", CaseRequest.class, "createElement");
        xstream.aliasField("update", CaseRequest.class, "updateElement");

        xstream.registerConverter(new PregnancyConverter());
        xstream.alias("index", Index.class);

        xstream.useAttributeFor(CaseRequest.class, "case_id");
        xstream.useAttributeFor(CaseRequest.class, "user_id");
        xstream.useAttributeFor(CaseRequest.class, "xmlns");
        xstream.useAttributeFor(CaseRequest.class, "date_modified");

        return xstream.toXML(request);
    }

    private CommcareRequestData createRequestWithEnvelope(CaseRequest caseRequest) {
        MetaElement metaElement = new MetaElement("http://openrosa.org/jr/xforms", UUID.randomUUID().toString(), caseRequest.getDate_modified(), caseRequest.getDate_modified(), caseRequest.getUser_id());
        return new CommcareRequestData("http://bihar.commcarehq.org/pregnancy/task", metaElement,caseRequest);    }

}