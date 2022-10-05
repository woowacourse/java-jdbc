package nextstep.mvc.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nextstep.mvc.HandlerMapping;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackages;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackages) {
        this.basePackages = basePackages;
        this.handlerExecutions = new HashMap<>();
    }

    @Override
    public void initialize() {
        final ControllerScanner controllerScanner = ControllerScanner.of(basePackages);
        final Map<Class<?>, Object> controllers = controllerScanner.getControllers();
        controllers.forEach(this::addHandlerExecutions);

        log.info("Initialized Handler Mapping!");
        handlerExecutions.keySet()
                .forEach(key -> log.info("{}, {}", key, handlerExecutions.get(key)));
    }

    private void addHandlerExecutions(final Class<?> controller, final Object instance) {
        for (final Method method : getRequestMappingMethods(controller)) {
            final HandlerExecution handlerExecution = new HandlerExecution(instance, method);
            final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            mapHandlerKeys(requestMapping.value(), requestMapping.method())
                    .forEach(hk -> handlerExecutions.put(hk, handlerExecution));
        }
    }

    private List<Method> getRequestMappingMethods(final Class<?> controller) {
        return Arrays.stream(controller.getMethods())
                .filter(m -> m.isAnnotationPresent(RequestMapping.class))
                .collect(Collectors.toList());
    }

    private Set<HandlerKey> mapHandlerKeys(final String url, final RequestMethod[] requestMethods) {
        return Arrays.stream(requestMethods)
                .map(rm -> new HandlerKey(url, rm))
                .collect(Collectors.toSet());
    }

    @Override
    public Object getHandler(final HttpServletRequest request) {
        final HandlerKey handlerKey = HandlerKey.from(request);
        return handlerExecutions.get(handlerKey);
    }
}
