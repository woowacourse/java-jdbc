package nextstep.mvc.controller.tobe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nextstep.web.WebApplicationContext;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Controller;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerScanner {

    private static final Logger log = LoggerFactory.getLogger(ControllerScanner.class);

    private final Reflections reflections;

    public ControllerScanner(Object... basePackage) {
        reflections = new Reflections(basePackage);
    }

    public Map<Class<?>, Object> getControllers(final WebApplicationContext webApplicationContext) {
        Set<Class<?>> preInitiatedControllers = reflections.getTypesAnnotatedWith(Controller.class);
        return instantiateControllers(preInitiatedControllers, webApplicationContext);
    }

    Map<Class<?>, Object> instantiateControllers(Set<Class<?>> preInitiatedControllers, WebApplicationContext webApplicationContext) {
        final Map<Class<?>, Object> controllers = new HashMap<>();
        try {
            for (Class<?> clazz : preInitiatedControllers) {
                Constructor<?> constructor = getConstructor(clazz);
                Object[] parametersOfConstructor = getParametersOfConstructor(webApplicationContext, constructor);
                controllers.put(clazz, constructor.newInstance(parametersOfConstructor));
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error(e.getMessage());
        }

        return controllers;
    }

    private Constructor<?> getConstructor(final Class<?> clazz) throws NoSuchMethodException {
        return Arrays.stream(clazz.getDeclaredConstructors())
            .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
            .findAny()
            .orElseGet(() -> {
                try {
                    return clazz.getDeclaredConstructor();
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(e);
                }
            });
    }

    private Object[] getParametersOfConstructor(final WebApplicationContext webApplicationContext, final Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameterTypes())
            .map(webApplicationContext::getBean)
            .toArray();
    }
}
