package nextstep.web;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.Repository;
import nextstep.web.annotation.Service;
import org.reflections.Reflections;

public class ComponentContainer {

    private static final Map<Class<?>, Object> COMPONENTS = new HashMap<>();

    private ComponentContainer() {}

    public static void initialize(DataSource dataSource, Object... basePackage) throws Exception {
        Reflections reflections = new Reflections(basePackage);
        initializeRepositories(reflections.getTypesAnnotatedWith(Repository.class), dataSource);
        initializeComponents(reflections.getTypesAnnotatedWith(Service.class));
        initializeComponents(reflections.getTypesAnnotatedWith(Controller.class));
    }

    private static void initializeRepositories(Set<Class<?>> repositoryTypes, DataSource dataSource) throws Exception {
        for (Class<?> repository : repositoryTypes) {
            Object instance = repository.getConstructor(DataSource.class).newInstance(dataSource);
            COMPONENTS.put(repository, instance);
        }
    }

    private static void initializeComponents(Set<Class<?>> componentTypes) throws Exception {
        for (Class<?> type : componentTypes) {
            Class<?>[] fieldTypes = getFieldTypes(type);
            Object[] fieldObjects = getFieldObjects(fieldTypes);

            Object instance = type.getConstructor(fieldTypes).newInstance(fieldObjects);
            COMPONENTS.put(type, instance);
        }
    }

    private static Class<?>[] getFieldTypes(Class<?> classTypes) {
        return Arrays.stream(classTypes.getDeclaredFields())
            .map(Field::getType)
            .filter(COMPONENTS::containsKey)
            .toArray(Class<?>[]::new);
    }

    private static Object[] getFieldObjects(Class<?>[] fieldTypes) {
        return Arrays.stream(fieldTypes)
            .map(COMPONENTS::get)
            .toArray(Object[]::new);
    }

    public static Object getInstance(Class<?> classType) {
        return COMPONENTS.get(classType);
    }
}
