package org.motechproject.casexml.service.exception;


import org.springframework.http.HttpStatus;

import java.util.Map;

public class CaseException extends RuntimeException {

    private HttpStatus httpStatusCode;
    private String message;
    private Map<String,String> errorMessages;

    public Map<String, String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(Map<String, String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public CaseException(Exception cause, HttpStatus httpStatusCode) {
        super(cause);
        this.message = cause.getMessage();
        this.httpStatusCode = httpStatusCode;
    }

    public CaseException(Exception cause, String message, HttpStatus httpStatusCode) {
        super(message, cause);
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
    
    public CaseException(Exception cause, String message, HttpStatus httpStatusCode, Map<String, String> errorMessages) {
        super(message, cause);
        this.message = message;
        this.httpStatusCode = httpStatusCode;
        this.errorMessages = errorMessages;
    }

    public CaseException(String message, HttpStatus httpStatusCode) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    public CaseException(String message, HttpStatus httpStatusCode, Map<String, String> errorMessages) {
        this.message = message;
        this.httpStatusCode = httpStatusCode;
        this.errorMessages = errorMessages;
    }

    public HttpStatus getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getMessage() {
        return message;
    }
}