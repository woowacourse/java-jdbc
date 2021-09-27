package nextstep.mvc.controller.tobe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

    public Map<Class<?>, Object> getControllers(Map<Class<?>, Object> repositories) {
        Set<Class<?>> preInitiatedControllers = reflections.getTypesAnnotatedWith(Controller.class);
        return instantiateControllers(preInitiatedControllers, repositories);
    }

    Map<Class<?>, Object> instantiateControllers(Set<Class<?>> preInitiatedControllers, Map<Class<?>, Object> repositories) {
        final Map<Class<?>, Object> controllers = new HashMap<>();
        try {
            for (Class<?> clazz : preInitiatedControllers) {
                Constructor<?> declaredConstructor = clazz.getConstructors()[0];
                if (declaredConstructor.getParameterCount() > 0) {
                    Class<?>[] parameterTypes = declaredConstructor.getParameterTypes();
                    List<Object> parameters = new ArrayList<>();
                    if (parameterTypes.length > 0) {
                        for (Class<?> parameterType : parameterTypes) {
                            if (repositories.containsKey(parameterType)) {
                                parameters.add(repositories.get(parameterType));
                            }
                        }
                        controllers.put(clazz, declaredConstructor.newInstance(parameters.toArray()));
                        continue;
                    }
                }

                controllers.put(clazz, declaredConstructor.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("error", e);
        }

        return controllers;
    }
}
