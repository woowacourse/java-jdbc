package com.interface21.webmvc.servlet.mvc;

import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.HandlerExecution;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HandlerExecutionRegistry {

    private final Map<HandlerKey, HandlerExecution> handlerExecutions = new HashMap<>();

    public void registerHandler(RequestMethod[] requestMethods, String requestUri, Method handlerMethod) {
        if (requestMethods.length == 0) {
            Arrays.stream(RequestMethod.values())
                    .forEach(requestMethod -> registerHandler(requestMethod, requestUri, handlerMethod));
            return;
        }
        for (RequestMethod requestMethod : requestMethods) {
            registerHandler(requestMethod, requestUri, handlerMethod);
        }
    }

    private void registerHandler(RequestMethod requestMethod, String requestUri, Method handlerMethod) {
        HandlerKey handlerKey = new HandlerKey(requestUri, requestMethod);
        if (handlerExecutions.containsKey(handlerKey)) {
            throw new IllegalStateException("Handler already registered for " + handlerKey);
        }
        HandlerExecution handlerExecution = new HandlerExecution(handlerMethod);
        handlerExecutions.put(handlerKey, handlerExecution);
    }

    public HandlerExecution getHandler(RequestMethod requestMethod, String requestUri) {
        HandlerKey handlerKey = new HandlerKey(requestUri, requestMethod);
        return handlerExecutions.get(handlerKey);
    }
}
