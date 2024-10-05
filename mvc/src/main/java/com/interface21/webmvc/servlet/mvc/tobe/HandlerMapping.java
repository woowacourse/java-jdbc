package com.interface21.webmvc.servlet.mvc.tobe;

import com.interface21.context.stereotype.Handler;
import jakarta.servlet.http.HttpServletRequest;

@Handler
public interface HandlerMapping {

    void initialize();

    Object getHandler(HttpServletRequest request);
}
