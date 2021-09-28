package nextstep.mvc.controller.tobe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.InjectDao;
import nextstep.web.annotation.Repository;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerScanner {

    private static final Logger log = LoggerFactory.getLogger(ControllerScanner.class);

    private final Reflections reflections;
    private final Map<Class<?>, Object> repositories;

    public ControllerScanner(Object... basePackage) {
        reflections = new Reflections(basePackage);
        repositories = getRepositories();
    }

    private Map<Class<?>, Object> getRepositories() {
        Reflections daoReflections = new Reflections("com.techcourse.dao");
        Set<Class<?>> preInitiatedRepositories = daoReflections
            .getTypesAnnotatedWith(Repository.class);
        return instantiateRepositories(preInitiatedRepositories);
    }

    private Map<Class<?>, Object> instantiateRepositories(Set<Class<?>> preInitiatedRepositories) {
        final Map<Class<?>, Object> repoMap = new HashMap<>();
        preInitiatedRepositories
            .forEach(repo -> {
                try {
                    repoMap.put(repo, repo.getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    log.error(e.getMessage(), e);
                }
            });
        return repoMap;
    }

    public Map<Class<?>, Object> getControllers() {
        Set<Class<?>> preInitiatedControllers = reflections.getTypesAnnotatedWith(Controller.class);
        return instantiateControllers(preInitiatedControllers);
    }

    Map<Class<?>, Object> instantiateControllers(Set<Class<?>> preInitiatedControllers) {
        final Map<Class<?>, Object> controllers = new HashMap<>();
        for (Class<?> clazz : preInitiatedControllers) {
            putClasses(controllers, clazz);
        }
        return controllers;
    }

    private void putClasses(Map<Class<?>, Object> controllers, Class<?> clazz) {
        try {
            log.info(clazz.getName());
            if (hasDao(clazz)) {
                final Constructor<?> declaredConstructor = Arrays
                    .stream(clazz.getDeclaredConstructors())
                    .filter(constructor -> constructor.isAnnotationPresent(InjectDao.class))
                    .findAny()
                    .orElseThrow();
                controllers.put(clazz, declaredConstructor.newInstance(findDao(clazz)));
                return;
            }
            controllers.put(clazz, clazz.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
        }
    }

    private boolean hasDao(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors())
            .anyMatch(constructor -> constructor.isAnnotationPresent(InjectDao.class));
    }

    private Object findDao(Class<?> clazz) {
        final Class<?> daoClass = Arrays.stream(clazz.getDeclaredFields())
            .map(Field::getType)
            .filter(c -> c.isAnnotationPresent(Repository.class))
            .findAny()
            .orElseThrow();
        log.info("Selected Dao Class : {}", daoClass.getName());
        return repositories.get(daoClass);
    }
}
