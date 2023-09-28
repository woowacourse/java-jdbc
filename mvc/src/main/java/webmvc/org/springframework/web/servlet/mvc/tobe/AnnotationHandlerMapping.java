package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.org.springframework.web.bind.annotation.RequestMapping;
import web.org.springframework.web.bind.annotation.RequestMethod;
import webmvc.org.springframework.web.servlet.mvc.HandlerMapping;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        final var controllerScanner = new ControllerScanner(basePackage);
        final var controllers = controllerScanner.getControllers();
        final var methods = getRequestMappingMethods(controllers.keySet());
        for (final var method : methods) {
            final var requestMapping = method.getAnnotation(RequestMapping.class);
            log.debug("register handlerExecution : url is {}, request method : {}, method is {}", requestMapping.value(), requestMapping.method(), method);
            addHandlerExecutions(controllers, method, requestMapping);
        }

        log.info("Initialized AnnotationHandlerMapping!");
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
