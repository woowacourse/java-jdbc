package com.techcourse;

import com.techcourse.controller.*;
import jakarta.servlet.http.HttpServletRequest;
import webmvc.org.springframework.web.servlet.mvc.HandlerMapping;
import webmvc.org.springframework.web.servlet.mvc.asis.Controller;
import webmvc.org.springframework.web.servlet.mvc.asis.ForwardController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ManualHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(ManualHandlerMapping.class);

    private static final Map<String, Controller> controllers = new HashMap<>();

    @Override
    public void initialize() {
        controllers.put("/", new ForwardController("/index.jsp"));
        controllers.put("/logout", new LogoutController());

        log.info("Initialized Handler Mapping!");

        for (Map.Entry<String, Controller> pathAndController : controllers.entrySet()) {
            String path = pathAndController.getKey();
            Controller controller = pathAndController.getValue();
            log.info("Path : {}, Controller : {}", path, controller.getClass());
        }
    }

    @Override
    public Controller getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.debug("Request Mapping Uri : {}", requestURI);
        return controllers.get(requestURI);
    }
}
