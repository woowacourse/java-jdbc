package com.interface21.webmvc.servlet.mvc.tobe;

import com.interface21.web.bind.annotation.RequestMapping;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandlerExecutions {

    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public HandlerExecutions() {
        this.handlerExecutions = new HashMap<>();
    }

    public void addHandlerExecution(Method[] methods) {
        Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .forEach(this::addHandlerExecution);
    }

    private void addHandlerExecution(Method method) {
        List<HandlerKey> handlerKeys = HandlerKeyExtractor.extract(method);
        handlerKeys.forEach(handlerKey -> handlerExecutions.put(handlerKey, new HandlerExecution(method)));
    }

    public boolean containsHandlerKey(HandlerKey handlerKey) {
        return handlerExecutions.containsKey(handlerKey);
    }

    public HandlerExecution get(HandlerKey handlerKey) {
        return handlerExecutions.get(handlerKey);
    }
}
