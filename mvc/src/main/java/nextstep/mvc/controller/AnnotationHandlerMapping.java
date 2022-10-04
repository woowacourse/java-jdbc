package nextstep.mvc.controller;

import static java.util.stream.Collectors.toList;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nextstep.mvc.HandlerMapping;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final String[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final String... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        log.info("Initialized AnnotationHandlerMapping!");
        initHandlerExecution(basePackage);
    }

    public Object getHandler(final HttpServletRequest request) {
        final HandlerKey handlerKey = new HandlerKey(request.getRequestURI(), RequestMethod.valueOf(request.getMethod()));
        return handlerExecutions.get(handlerKey);
    }

    private void initHandlerExecution(final Object[] basePackage) {
        final ControllerScanner controllerScanner = new ControllerScanner(new Reflections(basePackage));
        final Map<Class<?>, Object> controllers = controllerScanner.getControllers();

        for (Class<?> controller : controllers.keySet()) {
            final Set<Method> methods = getAllMethods(controller);
            putSupportedMethodInHandlerExecution(methods, controllers.get(controller));
        }
    }

    private static Set<Method> getAllMethods(final Class<?> aClass) {
        return ReflectionUtils.getAllMethods(aClass, ReflectionUtils.withAnnotation(RequestMapping.class));
    }

    private void putSupportedMethodInHandlerExecution(final Set<Method> methods, final Object handler) {
        for (Method method : methods) {
            putHandlerExecution(method, handler);
        }
    }

    private void putHandlerExecution(final Method method, final Object handler) {
        final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        final List<HandlerKey> handlerKeys = getHandlerKeys(requestMapping);

        for (HandlerKey handlerKey : handlerKeys) {
            log.info("HandlerKey : {}", handlerKey);
            handlerExecutions.put(handlerKey, new HandlerExecution(handler, method));
        }
    }

    private static List<HandlerKey> getHandlerKeys(final RequestMapping requestMapping) {
        if (requestMapping != null) {
            return Arrays.stream(requestMapping.method())
                    .map(method -> new HandlerKey(requestMapping.value(), method))
                    .collect(toList());
        }
        return new ArrayList<>();
    }
}
