package nextstep.mvc.handler.mapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import nextstep.mvc.handler.HandlerExecution;
import nextstep.mvc.handler.HandlerKey;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;

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
        scanControllers();
        log.info("Initialized AnnotationHandlerMapping!");
    }

    private void scanControllers() {
        ControllerScanner.getControllers(basePackages)
            .forEach(clazz -> parseHandlerInfo(RequestMappingScanner.getHandler(clazz)));
    }

    private void parseHandlerInfo(Set<Method> handlers) {
        for (Method handler : handlers) {
            RequestMapping requestMapping = handler.getAnnotation(RequestMapping.class);
            addHandler(requestMapping, requestMapping.value(), new HandlerExecution(handler));
        }
    }

    private void addHandler(RequestMapping requestMapping, String url, HandlerExecution handlerExecution) {
        for (RequestMethod requestMethod : requestMapping.method()) {
            handlerExecutions.put(new HandlerKey(url, requestMethod), handlerExecution);
        }
    }

    @Override
    public Optional<Object> getHandler(final HttpServletRequest request) {
        return Optional.ofNullable(handlerExecutions.get(HandlerKey.fromRequest(request)));
    }
}
