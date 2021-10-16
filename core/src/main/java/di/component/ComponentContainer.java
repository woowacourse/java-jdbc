package di.component;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import static org.reflections.ReflectionUtils.withAnnotations;

public class ComponentContainer {

    private final Map<Class<?>, Object> components;

    public ComponentContainer() {
        this.components = new HashMap<>();
    }

    public void register(Class<?> type, Object component) {
        components.put(type, component);
    }

    public Object takeComponent(Class<?> type) {
        return components.get(type);
    }

    public Map<Class<?>, Object> takeComponentsWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<Class<?>, Object> componentsWithAnnotation = new HashMap<>();
        for (Class<?> aClass : components.keySet()) {
            if (withAnnotations(annotationClass).apply(aClass)) {
                componentsWithAnnotation.put(aClass, components.get(aClass));
            }
        }
        return componentsWithAnnotation;
    }

    public Map<Class<?>, Object> getComponents() {
        return components;
    }
}
