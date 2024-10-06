package com.interface21.webmvc.servlet.mvc.tobe;

import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.mvc.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);
    private static final int DEFAULT_REQUEST_METHOD_COUNT = 0;

    private final Object[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    @Override
    public void initialize() {
        final ControllerScanner controllerScanner = new ControllerScanner(basePackage);
        final Map<Class<?>, Object> controllers = controllerScanner.getControllers();
        final Set<Method> methods = getRequestMappingMethods(controllers.keySet());
        for (final Method method : methods) {
            final Object controller = controllers.get(method.getDeclaringClass());
            addHandlerExecutions(controller, method);
        }
        log.info("Initialized AnnotationHandlerMapping!");
    }

    private Set<Method> getRequestMappingMethods(final Set<Class<?>> controllers) {
        return controllers.stream()
                .flatMap(clazz -> Arrays.stream(clazz.getMethods()))
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .collect(Collectors.toSet());
    }

    private void addHandlerExecutions(final Object controller, final Method method) {
        final HandlerExecution execution = new HandlerExecution(controller, method); // TODO: execution 객체 재사용 문제 고려
        final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        final String requestUrl = requestMapping.value();
        for (final RequestMethod requestMethod : getRequestMethods(requestMapping)) {
            final HandlerKey key = new HandlerKey(requestUrl, requestMethod);
            addHandlerExecution(key, execution);
        }
    }

    private RequestMethod[] getRequestMethods(final RequestMapping requestMapping) {
        final RequestMethod[] requestMethods = requestMapping.method();
        if (requestMethods.length == DEFAULT_REQUEST_METHOD_COUNT) {
            return RequestMethod.values();
        }
        return requestMethods;
    }

    private void addHandlerExecution(final HandlerKey key, final HandlerExecution execution) {
        if (handlerExecutions.containsKey(key)) {
            throw new IllegalArgumentException("이미 등록된 URL과 HTTP 메서드 조합입니다: " + key);
        }
        handlerExecutions.put(key, execution);
    }

    @Override
    public Object getHandler(final HttpServletRequest request) {
        final String requestUri = request.getRequestURI();
        final RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod().toUpperCase());
        final HandlerKey key = new HandlerKey(requestUri, requestMethod);
        return handlerExecutions.get(key);
    }
}
