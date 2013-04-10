package org.motechproject.server.verboice.web;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.motechproject.callflow.domain.CallDetailRecord;
import org.motechproject.callflow.domain.CallDetailRecord.Disposition;
import org.motechproject.callflow.domain.CallDirection;
import org.motechproject.callflow.domain.CallEvent;
import org.motechproject.callflow.domain.FlowSessionRecord;
import org.motechproject.callflow.service.CallFlowServer;
import org.motechproject.callflow.service.FlowSessionService;
import org.motechproject.decisiontree.core.FlowSession;
import org.motechproject.decisiontree.core.model.CallStatus;
import org.motechproject.ivr.service.SessionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

@Controller
public class VerboiceIVRController {

    private static final String VERBOICE_CALL_SID = "CallSid";
    private static final String MOTECH_CALL_ID = "motech_call_id";
    private static final String VERBOICE_FROM_PHONE_PARAM = "From";
    private Logger logger = Logger.getLogger(VerboiceIVRController.class);

    @Autowired
    private FlowSessionService flowSessionService;
    @Autowired
    private CallFlowServer callFlowServer;

    public VerboiceIVRController() {
    }

    @RequestMapping("/ivr")
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) {
        String verboiceCallId = request.getParameter(VERBOICE_CALL_SID);
        String motechCallId = request.getParameter(MOTECH_CALL_ID);
        String phoneNumber = request.getParameter(VERBOICE_FROM_PHONE_PARAM);
        FlowSession session = null;
        if (motechCallId == null) {
            session = flowSessionService.findOrCreate(verboiceCallId, phoneNumber);
            final CallDetailRecord callDetailRecord = ((FlowSessionRecord) session).getCallDetailRecord();
            callDetailRecord.setCallDirection(CallDirection.Inbound);
        } else {
            session = updateOutgoingCallSessionIdWithVerboiceSid(motechCallId, verboiceCallId);
        }

        String tree = request.getParameter("tree");
        String language = request.getParameter("ln");
        String digits = request.getParameter("DialCallStatus");
        if (StringUtils.isBlank(digits)) {
            digits = request.getParameter("Digits");
        }

        session.setLanguage(language);
        session = setCustomParams(session, request);
        flowSessionService.updateSession(session);

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        ModelAndView view = callFlowServer.getResponse(verboiceCallId, phoneNumber, "verboice", tree, digits, language);
        view.addObject("contextPath", request.getContextPath());
        view.addObject("servletPath", request.getServletPath());
        view.addObject("host", request.getHeader("Host"));
        view.addObject("scheme", request.getScheme());
        return view;
    }

    @RequestMapping(value = "/ivr/callstatus", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void handleMissedCall(HttpServletRequest request) {
        String callStatus = request.getParameter("CallStatus");
        String callSid = request.getParameter(VERBOICE_CALL_SID);
        logger.info("Verboice status callback : " + callStatus);

        updateRecord(callStatus, callSid);

        if ("completed".equals(callStatus)) {
            String language = request.getParameter("ln");
            String verboiceCallId = request.getParameter(VERBOICE_CALL_SID);
            String phoneNumber = request.getParameter(VERBOICE_FROM_PHONE_PARAM);
            String tree = request.getParameter("tree");
            callFlowServer.getResponse(verboiceCallId, phoneNumber, "verboice",tree, CallStatus.Disconnect.toString(), language);
        }

        List<String> missedCallStatuses = Arrays.asList("busy", "failed", "no-answer");
        if (callStatus == null || callStatus.trim().isEmpty() || !missedCallStatuses.contains(callStatus)) {
            return;
        }

        FlowSession session = flowSessionService.getSession(callSid);
        if (session == null) {
            throw new SessionNotFoundException("No session found! [Session Id " + callSid + "]");
        }

        callFlowServer.handleMissedCall(session.getSessionId());
    }

    private void updateRecord(String callStatus, String callSid) {
        FlowSessionRecord record = (FlowSessionRecord) flowSessionService.getSession(callSid);
        if (record != null) {

            CallDetailRecord callDetail = record.getCallDetailRecord();
            CallEvent callEvent = new CallEvent(callStatus);
            callDetail.addCallEvent(callEvent);

            if ("ringing".equals(callStatus)) {
                callDetail.setDisposition(Disposition.UNKNOWN);
            } else if ("in-progress".equals(callStatus)) {
                callDetail.setDisposition(Disposition.ANSWERED);
            } else if ("completed".equals(callStatus)) {
                callDetail.setDisposition(Disposition.ANSWERED);
            } else if ("failed".equals(callStatus)) {
                callDetail.setDisposition(Disposition.FAILED);
            } else if ("busy".equals(callStatus)) {
                callDetail.setDisposition(Disposition.BUSY);
            } else if ("no-answer".equals(callStatus)) {
                callDetail.setDisposition(Disposition.NO_ANSWER);
            }

            flowSessionService.updateSession(record);
        }
    }

    private FlowSession updateOutgoingCallSessionIdWithVerboiceSid(String callId, String verboiceCallId) {
        FlowSession flowSession = flowSessionService.getSession(callId);
        return flowSessionService.updateSessionId(flowSession.getSessionId(), verboiceCallId);
    }

    private FlowSession setCustomParams(FlowSession session, HttpServletRequest request) {
        Map params = request.getParameterMap();
        Set<String> keys = params.keySet();
        for (String key : keys) {
            if (!asList(VERBOICE_CALL_SID, "AccountSid", VERBOICE_FROM_PHONE_PARAM, "To", "CallStatus", "ApiVersion", "Direction", "ForwardedFrom", "CallerName", "FromCity", "FromState", "FromZip", "FromCountry", "ToCity", "ToState", "ToZip", "ToCountry", "ln").contains(key)) {
                session.set(key, (Serializable) params.get(key));
            }
        }
        return session;
    }
}
