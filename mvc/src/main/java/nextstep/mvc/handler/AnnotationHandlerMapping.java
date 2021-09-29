package nextstep.mvc.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nextstep.mvc.exception.InternalServerException;
import nextstep.mvc.scanner.ComponentScanner;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.RequestMapping;
import nextstep.web.support.RequestMethod;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        LOG.info("Initialized AnnotationHandlerMapping!");

        Reflections reflections = new Reflections(basePackage);
        ComponentScanner componentScanner = getComponentScanner(reflections);
        initHandleExecution(reflections, componentScanner);
    }

    private void initHandleExecution(Reflections reflections, ComponentScanner componentScanner) {
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Controller.class);
        typesAnnotatedWith.forEach(aClass -> {
            List<Method> declaredMethods = getRequestMappingMethods(aClass);
            addHandleExecution(componentScanner.getInstance(aClass), declaredMethods);
        });
    }

    private void addHandleExecution(Object instance, List<Method> methods) {
        for (Method aMethod : methods) {
            RequestMapping requestMapping = aMethod.getAnnotation(RequestMapping.class);
            String url = requestMapping.value();
            for (RequestMethod requestMethod : requestMapping.method()) {
                HandlerKey handlerKey = new HandlerKey(url, requestMethod);
                validateHandleKeyDuplicate(handlerKey);
                handlerExecutions.put(handlerKey, new HandlerExecution(instance, aMethod));
                LOG.info("Request Mapping Uri : {}", url);
            }
        }
    }

    private void validateHandleKeyDuplicate(HandlerKey handlerKey) {
        if (handlerExecutions.containsKey(handlerKey)) {
            throw new InternalServerException();
        }
    }

    private ComponentScanner getComponentScanner(Reflections reflections) {
        ComponentScanner componentScanner = new ComponentScanner(reflections);
        componentScanner.findComponent();
        return componentScanner;
    }

    private List<Method> getRequestMappingMethods(Class<?> aClass) {
        Method[] methods = aClass.getDeclaredMethods();
        return Arrays.stream(methods)
            .filter(method -> method.isAnnotationPresent(RequestMapping.class))
            .collect(Collectors.toList());
    }


    public Object getHandler(HttpServletRequest request) {
        final String requestUri = request.getRequestURI();
        final RequestMethod requestMethod = RequestMethod
            .valueOf(request.getMethod().toUpperCase());
        LOG.debug("requestUri : {}, requestMethod : {}", requestUri, requestMethod);
        return handlerExecutions.get(new HandlerKey(requestUri, requestMethod));
    }
}
