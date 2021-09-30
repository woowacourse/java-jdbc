package nextstep.web;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nextstep.web.annotation.Repository;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryScanner implements BeanScanner {

    private static final Logger log = LoggerFactory.getLogger(RepositoryScanner.class);

    private final Reflections reflections;

    public RepositoryScanner(final Object... baseObjects) {
        this.reflections = new Reflections(baseObjects);
    }

    @Override
    public Map<Class<?>, Object> scan() {
        Map<Class<?>, Object> repositories = new HashMap<>();
        Set<Class<?>> repositoryTypes = reflections.getTypesAnnotatedWith(Repository.class);

        for (Class<?> repositoryType : repositoryTypes) {
            try {
                Object repository = repositoryType.getDeclaredConstructor().newInstance();
                repositories.put(repositoryType, repository);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error(e.getMessage());
            }
        }

        return repositories;
    }
}
