package com.interface21.webmvc.servlet.mvc.handlermapping;

import com.interface21.bean.container.BeanContainer;
import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.mvc.HandlerExecution;
import com.interface21.webmvc.servlet.mvc.HandlerKey;
import com.interface21.webmvc.servlet.mvc.HandlerKeys;
import com.interface21.webmvc.servlet.mvc.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping() {
        this.handlerExecutions = new HashMap<>();
    }

    @Override
    public void initialize() {
        BeanContainer beanContainer = BeanContainer.getInstance();

        beanContainer.getAnnotatedBeans(Controller.class)
                .stream()
                .flatMap(bean -> Arrays.stream(bean.getClass().getMethods()))
                .filter(handler -> handler.isAnnotationPresent(RequestMapping.class))
                .forEach(this::addHandlers);
        log.info("Initialized AnnotationHandlerMapping!");
    }

    private void addHandlers(Method handler) {
        HandlerExecution handlerExecution = new HandlerExecution(handler);
        RequestMapping requestMapping = handler.getAnnotation(RequestMapping.class);
        HandlerKeys handlerKeys = HandlerKeys.from(requestMapping);

        handlerKeys.getKeys()
                .forEach(handlerKey -> addHandler(handlerKey, handlerExecution));
    }

    private void addHandler(HandlerKey handlerKey, HandlerExecution handlerExecution) {
        if (handlerExecutions.containsKey(handlerKey)) {
            throw new IllegalArgumentException("이미 존재하는 RequestMethod 입니다.");
        }
        handlerExecutions.put(handlerKey, handlerExecution);
    }

    @Override
    public Object getHandler(final HttpServletRequest request) {
        String url = request.getRequestURI();
        String method = request.getMethod();
        RequestMethod requestMethod = RequestMethod.from(method);
        HandlerKey handlerKey = new HandlerKey(url, requestMethod);

        validateHandlerKey(handlerKey);

        return handlerExecutions.get(handlerKey);
    }

    private void validateHandlerKey(HandlerKey handlerKey) {
        if (!handlerExecutions.containsKey(handlerKey)) {
            throw new IllegalArgumentException("지원하지 않는 요청입니다.");
        }
    }
}
