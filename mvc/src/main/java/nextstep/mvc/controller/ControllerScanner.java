package nextstep.mvc.controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nextstep.mvc.exception.ReflectionException;
import nextstep.web.annotation.Controller;
import org.reflections.Reflections;

public class ControllerScanner {

    private final Reflections reflections;

    private ControllerScanner(final Reflections reflections) {
        this.reflections = reflections;
    }

    public static ControllerScanner of(final Object... basePackages) {
        final Reflections reflections = new Reflections(basePackages);
        return new ControllerScanner(reflections);
    }

    public Map<Class<?>, Object> getControllers() {
        final Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        return controllers.stream()
                .collect(Collectors.toMap(c -> c, this::instantiateController));
    }

    private Object instantiateController(final Class<?> controller) {
        return getNewInstance(getConstructor(controller));
    }

    private Constructor<?> getConstructor(final Class<?> controllerClass) {
        try {
            return controllerClass.getConstructor();
        } catch (final NoSuchMethodException e) {
            throw new ReflectionException("A matching method is not found.", e);
        }
    }

    private Object getNewInstance(final Constructor<?> constructor) {
        try {
            return constructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException("Failed to create and initialize a new instance.", e);
        }
    }
}
