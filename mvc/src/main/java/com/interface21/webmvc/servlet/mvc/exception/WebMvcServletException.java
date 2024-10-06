package com.interface21.webmvc.servlet.mvc.exception;

import com.interface21.web.http.StatusCode;

public class WebMvcServletException extends RuntimeException {

    private final StatusCode statusCode;
    private final String message;

    public WebMvcServletException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
