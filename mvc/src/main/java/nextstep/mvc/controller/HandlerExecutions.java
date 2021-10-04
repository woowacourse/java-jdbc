package nextstep.mvc.controller;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nextstep.web.ComponentContainer;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerExecutions {

    private static final Logger LOG = LoggerFactory.getLogger(HandlerExecutions.class);

    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public HandlerExecutions() {
        this.handlerExecutions = new HashMap<>();
    }

    public void initializeWith(Set<Class<?>> handlers) {
        for (Class<?> handler : handlers) {
            initializeMethods(handler);
        }
    }

    private void initializeMethods(Class<?> handler) {
        Set<Method> annotatedMethods = getAnnotatedMethods(handler);
        Object instance = ComponentContainer.getInstance(handler);

        for (Method method : annotatedMethods) {
            initializeMethod(instance, method);
        }
    }

    private Set<Method> getAnnotatedMethods(Class<?> aClass) {
        return Arrays.stream(aClass.getMethods())
            .filter(method -> method.isAnnotationPresent(RequestMapping.class))
            .collect(Collectors.toSet());
    }

    private void initializeMethod(Object instance, Method method) {
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        String uri = requestMapping.value();

        for (RequestMethod requestMethod : requestMapping.method()) {
            HandlerKey handlerKey = new HandlerKey(uri, requestMethod);
            HandlerExecution handlerExecution = new HandlerExecution(instance, method);

            LOG.debug("Initialize Key: {}, Execution: {}", handlerKey, handlerExecution);
            handlerExecutions.put(handlerKey, handlerExecution);
        }
    }

    public Object getHandlerExecution(String uri, RequestMethod requestMethod) {
        HandlerKey handlerKey = new HandlerKey(uri, requestMethod);

        return handlerExecutions.get(handlerKey);
    }
}
