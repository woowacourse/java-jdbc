package com.techcourse;

import com.techcourse.controller.*;
import jakarta.servlet.http.HttpServletRequest;
import webmvc.org.springframework.web.servlet.mvc.asis.Controller;
import webmvc.org.springframework.web.servlet.mvc.asis.ForwardController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import webmvc.org.springframework.web.servlet.mvc.tobe.Handler;
import webmvc.org.springframework.web.servlet.mvc.tobe.HandlerMapping;
import webmvc.org.springframework.web.servlet.mvc.tobe.MannualHandler;

public class ManualHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(ManualHandlerMapping.class);

    private final Map<String, MannualHandler> handlers = new HashMap<>();

    @Override
    public void initialize() {
        handlers.put("/", new MannualHandler(new ForwardController("/index.jsp")));
        handlers.put("/logout", new MannualHandler(new LogoutController()));

        log.info("Initialized Handler Mapping!");
        handlers.keySet()
                .forEach(path -> log.info("Path : {}, Controller : {}", path, handlers.get(path).getClass()));
    }

    @Override
    public Handler getHandler(final HttpServletRequest request) {
        final var requestURI = request.getRequestURI();
        log.debug("Request Mapping Uri : {}", requestURI);
        return handlers.get(requestURI);
    }
}
