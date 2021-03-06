package org.motechproject.casexml.service;

import org.apache.log4j.Logger;
import org.motechproject.casexml.builder.ResponseMessageBuilder;
import org.motechproject.casexml.contract.CaseXmlRequest;
import org.motechproject.casexml.domain.CaseLog;
import org.motechproject.casexml.exception.CaseParserException;
import org.motechproject.casexml.parser.CommcareCaseParser;
import org.motechproject.casexml.service.exception.CaseException;
import org.motechproject.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public abstract class CaseService<T extends CaseXmlRequest> {

    private ResponseMessageBuilder responseMessageBuilder;

    private static Logger logger = Logger.getLogger(CaseService.class);

    private Class<T> clazz;
    private CaseLogService caseLogService;

    public CaseService(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Autowired
    public void setCaseLogService(CaseLogService caseLogService) {
        this.caseLogService = caseLogService;
    }

    @Autowired
    public void setResponseMessageBuilder(ResponseMessageBuilder responseMessageBuilder) {
        this.responseMessageBuilder = responseMessageBuilder;
    }

    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public ResponseEntity<String> processCase(HttpServletRequest request, HttpEntity<String> requestEntity) throws IOException {
        logger.info(requestEntity.getBody());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.TEXT_XML);

        CaseLog persistedLog = null;
        String requestId = null;
        String requestType = null;
        try {
            CommcareCaseParser<T> caseParser = new CommcareCaseParser<T>(clazz, requestEntity.getBody());
            T caseXmlRequest = caseParser.parseCase();
            if(caseXmlRequest != null) {
                requestId = caseXmlRequest.getId();
                requestType = caseXmlRequest.getType();
            }

            processCaseAction(caseParser, caseXmlRequest);
            persistedLog = createNewLog(requestId,  requestType, request.getPathInfo(), requestEntity.getBody(), false);
        } catch (CaseParserException exception) {
            logError(exception);
            persistedLog = createNewLog(requestId, requestType, request.getPathInfo(), requestEntity.getBody(), true);
            return loggedResponse(new ResponseEntity<String>(responseMessageBuilder.createResponseMessage(exception), responseHeaders, HttpStatus.BAD_REQUEST), persistedLog);

        } catch (CaseException exception) {
            logError(exception);
            persistedLog = createNewLog(requestId, requestType, request.getPathInfo(), requestEntity.getBody(), true);
            return loggedResponse(new ResponseEntity<String>(responseMessageBuilder.createResponseMessage(exception), responseHeaders, exception.getHttpStatusCode()), persistedLog);

        } catch (RuntimeException exception) {
            logError(exception);
            persistedLog = createNewLog(requestId, requestType, request.getPathInfo(), requestEntity.getBody(), true);
            return loggedResponse(new ResponseEntity<String>(responseMessageBuilder.messageForRuntimeException(), responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR), persistedLog);
        }
        return loggedResponse(new ResponseEntity<String>(responseMessageBuilder.messageForSuccess(), responseHeaders, HttpStatus.OK), persistedLog);
    }

    private CaseLog createNewLog(String requestId, String requestType, String requestURI, String requestBody, boolean hasException) {
        return new CaseLog(requestId, requestType, requestBody, requestURI, hasException, DateUtil.now().withMillisOfSecond(0));
    }

    private void log(CaseLog log) {
        caseLogService.add(log);
    }

    private void logError(Throwable exception) {
        logger.error("Exception encountered while processing case xml" + exception.getMessage() + exception.getStackTrace(), exception);
    }

    private ResponseEntity<String> loggedResponse(ResponseEntity<String> responseEntity, CaseLog caseLog) {
        logger.info("Sending case xml Response: Status Code: " + responseEntity.getStatusCode() + "; Body: " + responseEntity.getBody());
        caseLog.setResponse(responseEntity.getBody());
        log(caseLog);
        return responseEntity;
    }

    private void processCaseAction(CommcareCaseParser<T> caseParser, T object) throws CaseException {
        if ("CREATE".equals(caseParser.getCaseAction()))
            createCase(object);
        else if ("UPDATE".equals(caseParser.getCaseAction()))
            updateCase(object);
        else if ("CLOSE".equals(caseParser.getCaseAction()))
            closeCase(object);
    }


    public abstract void closeCase(T ccCase) throws CaseException;

    public abstract void updateCase(T ccCase) throws CaseException;

    public abstract void createCase(T ccCase) throws CaseException;

}
