package com.interface21.webmvc.servlet.mvc.exception;

import com.interface21.web.http.StatusCode;

public class NotSupportedHandlerAdapterException extends WebMvcServletException {

    public NotSupportedHandlerAdapterException(final Object handler) {
        super(StatusCode.INTERNAL_SERVER_ERROR, "해당 핸들러에 대한 어댑터를 제공하지 않습니다: " + handler);
    }
}
