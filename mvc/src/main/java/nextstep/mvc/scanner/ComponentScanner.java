package nextstep.mvc.scanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import nextstep.mvc.exception.InternalServerException;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.Repository;
import nextstep.web.annotation.Service;
import org.reflections.ReflectionUtils;
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
        instantiateComponent(reflections.getTypesAnnotatedWith(Repository.class));
        instantiateComponent(reflections.getTypesAnnotatedWith(Service.class));
        instantiateComponent(reflections.getTypesAnnotatedWith(Controller.class));
        initAutowired();
    }

    private void instantiateComponent(Set<Class<?>> components) {
        for (Class<?> component : components) {
            Object instance = createdInstance(component);
            beans.put(component, instance);
        }
    }

    private Object createdInstance(Class<?> component) {
        try {
            Constructor<?>[] declaredConstructors = component.getDeclaredConstructors();
            Optional<Constructor<?>> autowiredConstructor = getAutowiredConstructor(
                declaredConstructors);
            if (autowiredConstructor.isPresent()) {
                Constructor<?> constructor = autowiredConstructor.get();
                Object[] parameters = getParameters(constructor);
                LOG.info("Constructor Autowired : {}", parameters);
                return constructor.newInstance(parameters);
            }
            return component.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            LOG.error("Instance Create Error!! : {}", e.getMessage());
            throw new InternalServerException();
        }
    }

    private Optional<Constructor<?>> getAutowiredConstructor(
        Constructor<?>[] declaredConstructors) {
        return Arrays.stream(declaredConstructors)
            .filter(declaredConstructor -> declaredConstructor.isAnnotationPresent(Autowired.class))
            .findFirst();
    }

    private Object[] getParameters(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = beans.get(parameterTypes[i]);
        }
        return parameters;
    }

    private void initAutowired() {
        for (Entry<Class<?>, Object> entry : beans.entrySet()) {
            Set<Field> fields = ReflectionUtils.getAllFields(
                entry.getKey(),
                ReflectionUtils.withAnnotation(Autowired.class)
            );
            setFields(entry.getValue(), fields);
        }
    }

    private void setFields(Object instance, Set<Field> fields) {
        for (Field field : fields) {
            Class<?> fieldDeclaringClass = field.getType();
            Object value = beans.get(fieldDeclaringClass);
            try {
                field.setAccessible(true);
                field.set(instance, value);
                LOG.info("field AutoWried : {}", field.getType().getSimpleName());
            } catch (Exception e) {
                LOG.error("error : {}", e.getMessage());
            }
        }
    }

    public Object getInstance(Class<?> clazz) {
        return beans.get(clazz);
    }
}
