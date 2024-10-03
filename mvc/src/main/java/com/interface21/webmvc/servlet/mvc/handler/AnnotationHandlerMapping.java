package com.interface21.webmvc.servlet.mvc.handler;

import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerExecution;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerKey;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    @Override
    public void initialize() {
        log.info("Initialized AnnotationHandlerMapping!");
        scanRequestMappings();
    }

    private void scanRequestMappings() {
        Arrays.stream(basePackage)
                .forEach(this::scanControllerBy);
    }

    private void scanControllerBy(Object packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);
        controllerClasses.forEach(this::scanRequestMappingMethod);
    }

    private void scanRequestMappingMethod(Class<?> clazz) {
        try {
            Object controller = clazz.getDeclaredConstructor().newInstance();
            Arrays.stream(clazz.getMethods())
                    .filter(this::isRequestMappingMethod)
                    .forEach(method -> addHandlerExecution(controller, method));
        } catch (Exception e) {
            log.error(clazz.getName() + " 컨트롤러를 생성하지 못했습니다. ", e);
        }
    }

    private boolean isRequestMappingMethod(Method method) {
        return method.isAnnotationPresent(RequestMapping.class);
    }

    private void addHandlerExecution(Object controller, Method method) {
        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
        RequestMethod[] requestMethods = annotation.method();
        for (RequestMethod requestMethod : requestMethods) {
            HandlerKey handlerKey = new HandlerKey(annotation.value(), requestMethod);
            handlerExecutions.put(handlerKey, new HandlerExecution(controller, method));
        }
    }

    @Override
    public Object getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod());
        HandlerExecution handler = handlerExecutions.get(new HandlerKey(requestURI, requestMethod));
        if (handler == null) {
            String invalidRequestInfo = String.join(" ", requestMethod.name(), requestURI);
            throw new IllegalArgumentException("해당 요청을 처리할 수 있는 핸들러가 존재하지 않습니다. : " + invalidRequestInfo);
        }
        return handler;
    }
}
