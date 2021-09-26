package nextstep.mvc.controller.tobe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public Map<Class<?>, Object> getControllers() {
        Set<Class<?>> preInitiatedControllers = reflections.getTypesAnnotatedWith(Controller.class);
        return instantiateControllers(preInitiatedControllers);
    }

    Map<Class<?>, Object> instantiateControllers(Set<Class<?>> preInitiatedControllers) {
        final Map<Class<?>, Object> controllers = new HashMap<>();
        for (Class<?> clazz : preInitiatedControllers) {
            Object bean = create(clazz);
            controllers.put(clazz, bean);
        }
        return controllers;
    }

    private Object create(Class<?> clazz) {
        try {
            Constructor<?> constructor = findAutowiredConstructor(clazz);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] fields = getParametersForInjection(parameterTypes);
            return constructor.newInstance(fields);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException();
        }
    }

    private Constructor<?> findAutowiredConstructor(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors())
                     .filter(c -> c.isAnnotationPresent(Autowired.class))
                     .findAny()
                     .orElseGet(() -> {
                         try {
                             return clazz.getConstructor();
                         } catch (NoSuchMethodException e) {
                             throw new RuntimeException();
                         }
                     });
    }

    private Object[] getParametersForInjection(Class<?>[] parameterTypes) {
        Object[] fields = new Object[parameterTypes.length];
        int index = 0;
        for (Class<?> parameter : parameterTypes) {
            fields[index++] = create(parameter);
        }
        return fields;
    }
}
