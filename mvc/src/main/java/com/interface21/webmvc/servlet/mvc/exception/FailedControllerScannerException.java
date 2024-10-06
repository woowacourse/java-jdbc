package com.interface21.webmvc.servlet.mvc.exception;

import com.interface21.web.http.StatusCode;

public class FailedControllerScannerException extends WebMvcServletException {

    public FailedControllerScannerException(String messageDetail) {
        super(StatusCode.INTERNAL_SERVER_ERROR, "컨트롤러 클래스 인스턴스 생성에 실패했습니다: " + messageDetail);
    }
}
