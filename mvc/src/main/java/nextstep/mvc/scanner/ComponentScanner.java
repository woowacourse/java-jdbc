package nextstep.mvc.scanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import nextstep.mvc.exception.InternalServerException;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.Repository;
import nextstep.web.annotation.Service;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentScanner {

    private static final Logger LOG = LoggerFactory.getLogger(ComponentScanner.class);
    private final Map<Class<?>, Object> beans = new HashMap<>();

    private final Reflections reflections;

    public ComponentScanner(Reflections reflections) {
        this.reflections = reflections;
    }

    public void findComponent() {
        Set<Class<?>> components = reflections.getTypesAnnotatedWith(Controller.class);
        components.addAll(reflections.getTypesAnnotatedWith(Service.class));
        components.addAll(reflections.getTypesAnnotatedWith(Repository.class));
        instantiateComponent(components);
    }

    private void instantiateComponent(Set<Class<?>> components) {
        for (Class<?> component : components) {
            try {
                Object instance = component.getDeclaredConstructor().newInstance();
                beans.put(component, instance);
            } catch (Exception e) {
                LOG.error("Instance Create Error!! : {}", e.getMessage());
                throw new InternalServerException();
            }
        }
    }

    public Object getInstance(Class<?> clazz) {
        return beans.get(clazz);
    }

    public Map<Class<?>, Object> getBeans() {
        return beans;
    }
}
