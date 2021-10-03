package nextstep.mvc.controller.tobe;

import nextstep.mvc.controller.tobe.exception.InstanceCreationFailureException;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Bean;
import nextstep.web.annotation.Configuration;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.Repository;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentScanner {

    private final Reflections reflections;
    private final Map<Class<?>, Object> container;

    public ComponentScanner(Object... basePackage) {
        reflections = new Reflections(basePackage);
        container = new HashMap<>();
        scanConfigurations();
        scanRepositories();
        scanControllers();
    }

    private void scanConfigurations() {
        Set<Class<?>> configurations = reflections.getTypesAnnotatedWith(Configuration.class);
        for (Class<?> configuration : configurations) {
            Arrays.stream(configuration.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Bean.class))
                    .forEach(method -> container.put(method.getReturnType(), invokeMethod(configuration, method)));
        }
    }

    private Object invokeMethod(Class<?> configuration, Method method) {
        try {
            return method.invoke(instantiate(configuration));
        } catch (ReflectiveOperationException exception) {
            throw new InstanceCreationFailureException(exception);
        }
    }

    private void scanRepositories() {
        Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(Repository.class);
        for (Class<?> repository : repositories) {
            container.put(repository, instantiateClass(repository));
        }
    }

    private void scanControllers() {
        Set<Class<?>> preInitiatedControllers = reflections.getTypesAnnotatedWith(Controller.class);
        for (Class<?> controller : preInitiatedControllers) {
            container.put(controller, instantiateClass(controller));
        }
    }

    public Map<Class<?>, Object> getControllers() {
        return container.entrySet().stream()
                .filter(entry -> entry.getKey().isAnnotationPresent(Controller.class))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Object instantiateClass(Class<?> controllerClass) {
        return Arrays.stream(controllerClass.getDeclaredConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .findFirst()
                .map(this::instantiateWithDependencyInjected)
                .orElseGet(() -> instantiate(controllerClass));
    }

    private Object instantiateWithDependencyInjected(Constructor<?> autowiredControllerConstructor) {
        Object[] objects = Arrays.stream(autowiredControllerConstructor.getParameterTypes())
                .map(container::get)
                .toArray();

        return instantiate(autowiredControllerConstructor, objects);
    }

    private Object instantiate(Class<?> clazz) {
        try {
            return instantiate(clazz.getConstructor());
        } catch (ReflectiveOperationException exception) {
            throw new InstanceCreationFailureException(exception);
        }
    }

    private Object instantiate(Constructor<?> constructor, Object... parameterInstances) {
        try {
            return constructor.newInstance(parameterInstances);
        } catch (ReflectiveOperationException exception) {
            throw new InstanceCreationFailureException(exception);
        }
    }
}
