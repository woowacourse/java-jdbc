package nextstep.mvc.controller.tobe;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nextstep.web.annotation.Controller;

/**
 * Scan all classes annotated with @Controller within given basePackage and instantiate with default constructor
 */
public class ControllerScanner {

    private static final Logger log = LoggerFactory.getLogger(ControllerScanner.class);

    private final Reflections reflections;

    public ControllerScanner(final Object... basePackage) {
        reflections = new Reflections(basePackage);
    }

    public Map<Class<?>, Object> getControllers() {
        final var beforeInitiatedControllers = reflections.getTypesAnnotatedWith(Controller.class);
        return instantiateControllers(beforeInitiatedControllers);
    }

    private Map<Class<?>, Object> instantiateControllers(final Set<Class<?>> preInitiatedControllers) {
        final var controllers = new HashMap<Class<?>, Object>();
        for (final var clazz : preInitiatedControllers) {
            controllers.put(clazz, instantiateController(clazz));
        }

        return controllers;
    }

    private Object instantiateController(final Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException("controller instantiation failed");
        }
    }
}
