package nextstep.mvc.controller.tobe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nextstep.web.annotation.Service;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceScanner {

    private static final Logger log = LoggerFactory.getLogger(ServiceScanner.class);

    private final Reflections reflections;

    public ServiceScanner(Reflections reflections) {
        this.reflections = reflections;
    }

    public Map<Class<?>, Object> getServices() {
        Set<Class<?>> preInitiatedServices = reflections.getTypesAnnotatedWith(Service.class);
        RepositoryScanner repositoryScanner = new RepositoryScanner(reflections);
        Map<Class<?>, Object> repositories = repositoryScanner.getRepositories();
        return instantiateServices(preInitiatedServices, repositories);
    }

    private Map<Class<?>, Object> instantiateServices(Set<Class<?>> preInitiatedServices,
        Map<Class<?>, Object> repositories) {
        final Map<Class<?>, Object> services = new HashMap<>();
        try {
            for (Class<?> clazz : preInitiatedServices) {
                fillServiceRequirement(repositories, services, clazz);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage());
        }

        return services;
    }

    private void fillServiceRequirement(Map<Class<?>, Object> repositories,
        Map<Class<?>, Object> services, Class<?> clazz)
        throws InstantiationException, IllegalAccessException, InvocationTargetException {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {

            Object[] requiredParameters = constructor.getParameterTypes();
            List<Object> pairedParameters = new LinkedList<>();

            fillServiceConstructorRequirement(repositories, requiredParameters, pairedParameters);
            services.put(clazz, constructor.newInstance(pairedParameters.toArray()));
        }
    }

    private void fillServiceConstructorRequirement(Map<Class<?>, Object> repositories,
        Object[] requiredParameters,
        List<Object> pairedParameters) {
        for (Object requiredParameter : requiredParameters) {
            Object matchedParameter = repositories.get(requiredParameter);
            pairedParameters.add(matchedParameter);
        }
    }

}
