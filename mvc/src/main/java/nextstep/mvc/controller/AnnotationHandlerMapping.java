package nextstep.mvc.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import nextstep.mvc.HandlerMapping;
import nextstep.web.annotation.Controller;
import nextstep.web.support.RequestMethod;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final HandlerExecutions handlerExecutions;

    public AnnotationHandlerMapping(Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HandlerExecutions();
    }

    @Override
    public void initialize() {
        try {
            Reflections reflections = new Reflections(basePackage, new TypeAnnotationsScanner(), new SubTypesScanner());
            Set<Class<?>> annotatedHandlers = reflections.getTypesAnnotatedWith(Controller.class);

            LOG.info("Initialized AnnotationHandlerMapping!");
            handlerExecutions.initializeWith(annotatedHandlers);
        } catch (Exception e) {
            LOG.error("Failed Initialized AnnotationHandlerMapping...\n{}", e.getMessage());
        }
    }

    @Override
    public Object getHandler(HttpServletRequest request) {
        String uri = request.getRequestURI();
        RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod());

        return handlerExecutions.getHandlerExecution(uri, requestMethod);
    }
}
