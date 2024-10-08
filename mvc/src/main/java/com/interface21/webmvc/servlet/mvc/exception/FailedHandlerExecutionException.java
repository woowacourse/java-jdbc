package com.interface21.webmvc.servlet.mvc.exception;

import com.interface21.web.http.StatusCode;
import java.lang.reflect.Method;

public class FailedHandlerExecutionException extends WebMvcServletException {

    public FailedHandlerExecutionException(Method method, String messageDetail) {
        super(StatusCode.INTERNAL_SERVER_ERROR, method.getName() + "() method invoke에 실패했습니다: " + messageDetail);
    }
}
