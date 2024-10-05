package com.interface21.webmvc.servlet.mvc.tobe;

import com.interface21.HandlerContainer;
import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final HandlerExecutions handlerExecutions;

    public AnnotationHandlerMapping() {
        this.handlerExecutions = new HandlerExecutions();
    }

    @Override
    public void initialize() {
        HandlerContainer handlerContainer = HandlerContainer.getInstance();
        handlerContainer.getHandlerWithAnnotation(Controller.class).stream()
                .map(object -> object.getClass().getDeclaredMethods())
                .forEach(handlerExecutions::addHandlerExecution);

        log.info("Initialized AnnotationHandlerMapping!");
    }

    @Override
    public Object getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        HandlerKey handlerKey = new HandlerKey(requestURI, RequestMethod.valueOf(requestMethod));
        if (handlerExecutions.containsHandlerKey(handlerKey)) {
            return handlerExecutions.get(handlerKey);
        }
        throw new IllegalArgumentException("일치하는 handlerkey가 없습니다");
    }
}
