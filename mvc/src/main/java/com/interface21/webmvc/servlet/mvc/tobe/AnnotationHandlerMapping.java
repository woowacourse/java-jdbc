package com.interface21.webmvc.servlet.mvc.tobe;

import com.interface21.core.util.DIContainer;
import com.interface21.webmvc.servlet.mvc.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final DIContainer diContainer;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final DIContainer diContainer, final Object... basePackage) {
        this.diContainer = diContainer;
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        final ControllerScanner controllerScanner = new ControllerScanner(basePackage);
        final Map<Class<?>, Object> controllers = createDiCompletedControllers(controllerScanner);
        final var methods = getRequestMappingMethods(controllers.keySet());
        for (final var method : methods) {
            final var requestMapping = method.getAnnotation(RequestMapping.class);
            log.debug("register handlerExecution : url is {}, request method : {}, method is {}", requestMapping.value(), requestMapping.method(), method);
            addHandlerExecutions(controllers, method, requestMapping);
        }

        log.info("Initialized AnnotationHandlerMapping!");
    }

    private Map<Class<?>, Object> createDiCompletedControllers(ControllerScanner controllerScanner) {
        final Map<Class<?>, Object> controllersWithDI = new HashMap<>();

        for (final var clazz : controllerScanner.getControllers().keySet()) {
            final var diCompletedInstance = diContainer.getBean(clazz);

            controllersWithDI.put(clazz, diCompletedInstance);
        }
        return controllersWithDI;
    }


    private void addHandlerExecutions(final Map<Class<?>, Object> controllers, final Method method, final RequestMapping rm) {
        final var handlerKeys = mapHandlerKeys(rm.value(), rm.method());
        handlerKeys.forEach(handlerKey -> {
            handlerExecutions.put(handlerKey, new HandlerExecution(controllers.get(method.getDeclaringClass()), method));
        });
    }

    private List<HandlerKey> mapHandlerKeys(final String value, final RequestMethod[] originalMethods) {
        var targetMethods = originalMethods;
        if (targetMethods.length == 0) {
            targetMethods = RequestMethod.values();
        }
        return Arrays.stream(targetMethods)
                .map(method -> new HandlerKey(value, method))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Set<Method> getRequestMappingMethods(final Set<Class<?>> controllers) {
        final var requestMappingMethods = new HashSet<Method>();
        for (final var clazz : controllers) {
            requestMappingMethods
                    .addAll(ReflectionUtils.getAllMethods(clazz, ReflectionUtils.withAnnotation(RequestMapping.class)));
        }
        return requestMappingMethods;
    }

    public Object getHandler(final HttpServletRequest request) {
        final var requestUri = request.getRequestURI();
        final var requestMethod = RequestMethod.valueOf(request.getMethod().toUpperCase());
        log.debug("requestUri : {}, requestMethod : {}", requestUri, requestMethod);
        return handlerExecutions.get(new HandlerKey(requestUri, requestMethod));
    }
}
