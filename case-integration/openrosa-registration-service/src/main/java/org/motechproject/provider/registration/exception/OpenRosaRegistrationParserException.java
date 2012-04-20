package org.motechproject.provider.registration.exception;

import org.springframework.http.HttpStatus;

public class OpenRosaRegistrationParserException extends Exception {
    
    private HttpStatus statusCode;
    private String message;

    public OpenRosaRegistrationParserException(Exception cause, HttpStatus statusCode) {
        super(cause);
        this.message = cause.getMessage();
        this.statusCode = statusCode;
    }
    
    public OpenRosaRegistrationParserException(Exception cause, String message, HttpStatus statusCode) {
        super(message, cause);
        this.message = message;
        this.statusCode = statusCode;
    }
    
    public OpenRosaRegistrationParserException(String message, HttpStatus statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}