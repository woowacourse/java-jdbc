package di.component;

import java.util.HashMap;
import java.util.Map;

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

    public Map<Class<?>, Object> getComponents() {
        return components;
    }
}
