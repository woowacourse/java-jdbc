package com.interface21.webmvc.servlet.mvc;

import com.interface21.context.stereotype.Controller;
import com.interface21.core.BeanContainer;
import com.interface21.core.BeanContainerFactory;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import com.interface21.webmvc.servlet.HandlerExecution;
import com.interface21.webmvc.servlet.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final HandlerExecutionRegistry handlerExecutionRegistry;

    public AnnotationHandlerMapping() {
        this.handlerExecutionRegistry = new HandlerExecutionRegistry();
    }

    @Override
    public void initialize() {
        BeanContainer beanContainer = BeanContainerFactory.getContainer();
        beanContainer.getAnnotatedBeans(Controller.class)
                .forEach(this::registerController);
        log.info("Initialized AnnotationHandlerMapping!");
    }

    private void registerController(Object controller) {
        Arrays.stream(controller.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .forEach(this::registerHandlerExecution);
    }

    private void registerHandlerExecution(Method handlerMethod) {
        RequestMapping requestMapping = handlerMethod.getAnnotation(RequestMapping.class);
        RequestMethod[] methods = requestMapping.method();
        String requestUri = requestMapping.value();
        handlerExecutionRegistry.registerHandler(methods, requestUri, handlerMethod);
    }

    @Override
    public HandlerExecution getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod());
        return handlerExecutionRegistry.getHandler(requestMethod, requestURI);
    }
}
