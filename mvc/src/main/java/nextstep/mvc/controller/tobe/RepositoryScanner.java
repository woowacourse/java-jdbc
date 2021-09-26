package nextstep.mvc.controller.tobe;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import nextstep.web.annotation.Repository;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryScanner {

    private static final Logger log = LoggerFactory.getLogger(RepositoryScanner.class);

    private final Reflections reflections;

    public RepositoryScanner(Reflections reflections) {
        this.reflections = reflections;
    }

    public Map<Class<?>, Object> getRepositories() {
        Set<Class<?>> preInitiatedRepositories = reflections.getTypesAnnotatedWith(
            Repository.class);
        return instantiateRepositories(preInitiatedRepositories);
    }

    private Map<Class<?>, Object> instantiateRepositories(Set<Class<?>> preInitiatedRepositories) {
        final Map<Class<?>, Object> repositories = new HashMap<>();

        try {
            DBConnector dbConnector = new DBConnector(reflections);
            DataSource dataSource = dbConnector.getDataSource();
            for (Class<?> daoClass : preInitiatedRepositories) {
                repositories.put(daoClass, daoClass.getDeclaredConstructor(DataSource.class).newInstance(dataSource));
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error(e.getMessage());
        }
        return repositories;
    }
}
