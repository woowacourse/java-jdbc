package nextstep.web;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nextstep.mvc.exception.ComponentContainerException;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Configuration;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.Initialize;
import nextstep.web.annotation.Repository;
import nextstep.web.annotation.Service;
import org.reflections.Reflections;

public class ComponentContainer {

    private static final Map<Class<?>, Object> COMPONENTS = new HashMap<>();

    private ComponentContainer() {}

    public static void initialize(Object... basePackage) throws Exception {
        Reflections reflections = new Reflections(basePackage);
        initializeConfigurations(reflections.getTypesAnnotatedWith(Configuration.class));
        initializeComponents(reflections.getTypesAnnotatedWith(Repository.class));
        initializeComponents(reflections.getTypesAnnotatedWith(Service.class));
        initializeComponents(reflections.getTypesAnnotatedWith(Controller.class));
    }

    private static void initializeConfigurations(Set<Class<?>> configurationTypes) throws Exception {
        for (Class<?> configuration : configurationTypes) {
            List<Method> initializeMethods = Arrays.stream(configuration.getMethods())
                .filter(method -> method.isAnnotationPresent(Initialize.class))
                .collect(Collectors.toList());

            Constructor<?> constructor = configuration.getDeclaredConstructor();
            constructor.setAccessible(true);
            initializeConfigurationMethods(constructor.newInstance(), initializeMethods);
        }
    }

    private static void initializeConfigurationMethods(Object instance, List<Method> initializeMethods) throws Exception {
        for (Method method : initializeMethods) {
            COMPONENTS.put(method.getReturnType(), method.invoke(instance));
        }
    }

    private static void initializeComponents(Set<Class<?>> componentTypes) throws Exception {
        for (Class<?> type : componentTypes) {
            Constructor<?> constructor = getConstructor(type);
            Object[] parameterObjects = getConstructorParameterObjects(constructor);

            COMPONENTS.put(type, constructor.newInstance(parameterObjects));
        }
    }

    private static Constructor<?> getConstructor(Class<?> type) {
        return Arrays.stream(type.getConstructors())
            .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
            .findAny()
            .orElseGet(() -> getDefaultConstructor(type));
    }

    private static Constructor<?> getDefaultConstructor(Class<?> type) {
        try {
            return type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ComponentContainerException("기본 생성자 탐색에 실패했습니다.");
        }
    }

    private static Object[] getConstructorParameterObjects(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameterTypes())
            .map(COMPONENTS::get)
            .toArray(Object[]::new);
    }

    public static Object getInstance(Class<?> classType) {
        return COMPONENTS.get(classType);
    }
}
