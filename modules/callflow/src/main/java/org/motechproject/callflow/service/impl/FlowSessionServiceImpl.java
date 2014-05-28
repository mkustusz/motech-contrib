package org.motechproject.callflow.service.impl;

import org.apache.commons.lang.StringUtils;
import org.motechproject.callflow.domain.FlowSessionRecord;
import org.motechproject.callflow.repository.AllFlowSessionRecords;
import org.motechproject.callflow.service.FlowSessionService;
import org.motechproject.decisiontree.model.FlowSession;
import org.motechproject.ivr.service.contract.CallRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the <code>FlowSessionService</code> interface. Allows manipulation and retrieval of flow sessions.
 */
@Service("flowSessionService")
public class FlowSessionServiceImpl implements FlowSessionService {

    private AllFlowSessionRecords allFlowSessionRecords;
    private CallRecordsService callRecordsService;

    @Autowired
    public FlowSessionServiceImpl(AllFlowSessionRecords allFlowSessionRecords, CallRecordsService callRecordsService) {
        this.allFlowSessionRecords = allFlowSessionRecords;
        this.callRecordsService = callRecordsService;
    }

    @Override
    public FlowSession findOrCreate(String sessionId, String phoneNumber) {
        String flowSessionId = (StringUtils.isBlank(sessionId)) ? UUID.randomUUID().toString() : sessionId;
        return allFlowSessionRecords.findOrCreate(flowSessionId, phoneNumber);
    }

    @ResponseBody
    @Override
    public FlowSession getSession(String sessionId) {
        return allFlowSessionRecords.findBySessionId(sessionId);
    }

    @Override
    public void updateSession(FlowSession flowSession) {
        callRecordsService.add(flowSession.getCallDetailRecord());
        allFlowSessionRecords.update((FlowSessionRecord) flowSession);
    }

    @Override
    public void removeCallSession(String sessionId) {
        FlowSessionRecord flowSessionRecord = allFlowSessionRecords.findBySessionId(sessionId);
        if (flowSessionRecord != null) {
            allFlowSessionRecords.remove(flowSessionRecord);
        }
    }

    @Override
    public boolean isValidSession(String sessionId) {
        return allFlowSessionRecords.findBySessionId(sessionId) != null;
    }

    @Override
    public FlowSession updateSessionId(String sessionId, String newSessionId) {
        FlowSessionRecord flowSession = allFlowSessionRecords.findBySessionId(sessionId);
        flowSession.setSessionId(newSessionId);
        allFlowSessionRecords.update(flowSession);
        return flowSession;
    }

    @Override
    public FlowSession getSession(HttpServletRequest request, String language, String phoneNumber) {
        String sessionId = request.getParameter(FLOW_SESSION_ID_PARAM);
        return copyParameters(request, findOrCreate(sessionId, phoneNumber));
    }

    // TODO: session should not have provider specific params
    private FlowSession copyParameters(HttpServletRequest request, FlowSession session) {
        Map <String, Object> parameters = request.getParameterMap();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            session.set(entry.getKey().toString(), ((String[]) entry.getValue())[0]);
        }
        return session;
    }
}
