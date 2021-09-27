package nextstep.mvc.controller.tobe;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nextstep.web.annotation.Repository;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryScanner {

    private static final Logger log = LoggerFactory.getLogger(RepositoryScanner.class);


    private final Reflections reflections;

    public RepositoryScanner(Object... basePackage) {
        reflections = new Reflections(basePackage);
    }

    public Map<Class<?>, Object> getRepositories() {
        Set<Class<?>> preInitiatedRepositories = reflections.getTypesAnnotatedWith(Repository.class);
        return instantiateRepositories(preInitiatedRepositories);
    }

    Map<Class<?>, Object> instantiateRepositories(Set<Class<?>> preInitiatedRepositories) {
        final Map<Class<?>, Object> repositories = new HashMap<>();
        try {
            for (Class<?> clazz : preInitiatedRepositories) {
                repositories.put(clazz, clazz.getDeclaredConstructor().newInstance());
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error(e.getMessage());
        }

        return repositories;
    }
}
