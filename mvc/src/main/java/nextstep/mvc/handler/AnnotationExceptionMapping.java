package nextstep.mvc.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nextstep.mvc.scanner.ControllerAdviceScanner;
import nextstep.web.annotation.ExceptionHandler;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationExceptionMapping implements ExceptionMapping {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationExceptionMapping.class);

    private final Object[] basePackage;
    private final Map<Class<?>, ExceptionHandlerExecution> handlerExecutions = new HashMap<>();

    public AnnotationExceptionMapping(Object... basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public void initialize() {
        LOG.info("Initialized AnnotationExceptionHandlerMapping!");
        Reflections reflections = new Reflections(basePackage);
        ControllerAdviceScanner controllerAdviceScanner = getControllerAdviceScanner(reflections);
        initHandleExecution(controllerAdviceScanner);
    }

    private void initHandleExecution(ControllerAdviceScanner controllerAdviceScanner) {
        Set<Class<?>> controllerAdvices = controllerAdviceScanner.getControllerAdviceClasses();
        for (Class<?> clazz : controllerAdvices) {
            Method[] methods = clazz.getDeclaredMethods();
            Object instance = controllerAdviceScanner.getInstance(clazz);
            addHandleExecution(instance, methods);
        }
    }

    private void addHandleExecution(Object instance, Method[] methods) {
        for (Method method : methods) {
            ExceptionHandler annotation = method.getAnnotation(ExceptionHandler.class);
            Class<?> value = annotation.value();
            ExceptionHandlerExecution exceptionHandlerExecution =
                new ExceptionHandlerExecution(instance, method);
            handlerExecutions.put(value, exceptionHandlerExecution);
        }
    }

    private ControllerAdviceScanner getControllerAdviceScanner(Reflections reflections) {
        ControllerAdviceScanner controllerAdviceScanner = new ControllerAdviceScanner(reflections);
        controllerAdviceScanner.findControllerAdvice();
        return controllerAdviceScanner;
    }

    @Override
    public Object getHandler(RuntimeException exception) {
        return handlerExecutions.get(exception.getClass());
    }
}
