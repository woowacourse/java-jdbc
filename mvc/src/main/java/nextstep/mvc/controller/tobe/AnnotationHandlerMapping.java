package nextstep.mvc.controller.tobe;

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

import jakarta.servlet.http.HttpServletRequest;
import nextstep.mvc.HandlerMapping;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        final var controllers = new ControllerScanner(basePackage).getControllers();
        final var methods = getRequestMappingMethods(controllers.keySet());
        for (final var method : methods) {
            final var requestMapping = method.getAnnotation(RequestMapping.class);
            log.debug("register handlerExecution : url is {}, request method : {}, method is {}",
                requestMapping.value(), requestMapping.method(), method);
            addHandlerExecutions(controllers, method, requestMapping);
        }

        log.info("Initialized AnnotationHandlerMapping!");
    }

    private void addHandlerExecutions(final Map<Class<?>, Object> controllers, final Method method,
        final RequestMapping requestMapping) {
        final List<HandlerKey> handlerKeys = mapHandlerKeys(requestMapping.value(), requestMapping.method());
        handlerKeys.forEach(handlerKey -> {
            handlerExecutions.put(handlerKey,
                new HandlerExecution(controllers.get(method.getDeclaringClass()), method));
        });
    }

    private List<HandlerKey> mapHandlerKeys(final String value, final RequestMethod[] originalMethods) {
        return Arrays.stream(getTargetMethods(originalMethods))
            .map(method -> new HandlerKey(value, method))
            .collect(Collectors.toList());
    }

    private RequestMethod[] getTargetMethods(final RequestMethod[] originalMethods) {
        if (originalMethods.length == 0) {
            return RequestMethod.values();
        }
        return originalMethods;
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
