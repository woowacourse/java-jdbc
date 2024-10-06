package com.interface21.webmvc.servlet.mvc.exception;

import com.interface21.web.http.StatusCode;

public class NotFoundHandlerException extends WebMvcServletException {

    public NotFoundHandlerException() {
        super(StatusCode.NOT_FOUND, "요청에 맞는 핸들러를 찾을 수 없습니다.");
    }
}
