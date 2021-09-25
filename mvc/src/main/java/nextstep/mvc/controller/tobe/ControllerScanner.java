package nextstep.mvc.controller.tobe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.Inject;
import nextstep.web.annotation.Repository;
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
        try {
            for (Class<?> clazz : preInitiatedControllers) {
                if (checkInjection(clazz)) {
                    Constructor<?> constructor = getInjectConstructor(clazz);
                    controllers.put(clazz, constructor.newInstance(getDaoInstance()));
                    continue;
                }
                controllers.put(clazz, clazz.getDeclaredConstructor().newInstance());
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error(e.getMessage());
        }

        return controllers;
    }

    private Constructor<?> getInjectConstructor(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors())
            .filter(c -> c.isAnnotationPresent(Inject.class))
            .findAny()
            .orElseThrow();
    }

    private boolean checkInjection(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors())
            .anyMatch(constructor -> constructor.isAnnotationPresent(Inject.class));
    }

    private Object getDaoInstance()
        throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Reflections reflectionsDao = new Reflections("com.techcourse.dao");
        Set<Class<?>> daoClasses = reflectionsDao
            .getTypesAnnotatedWith(Repository.class);

        Class<?> daoClazz = daoClasses.stream()
            .findFirst()
            .orElseThrow();

        return daoClazz.getDeclaredConstructor().newInstance();
    }
}
