package com.techcourse;

import com.interface21.context.stereotype.Component;
import com.interface21.webmvc.servlet.mvc.Controller;
import com.interface21.webmvc.servlet.mvc.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ManualHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(ManualHandlerMapping.class);

    private static final Map<String, Controller> controllers = new HashMap<>();

    @Override
    public void initialize() {
        log.info("Initialized Handler Mapping!");
        controllers.keySet()
                .forEach(path -> log.info("Path : {}, Controller : {}", path, controllers.get(path).getClass()));
    }

    @Override
    public Controller getHandler(final HttpServletRequest request) {
        final var requestURI = request.getRequestURI();
        log.debug("Request Mapping Uri : {}", requestURI);
        return controllers.get(requestURI);
    }
}
