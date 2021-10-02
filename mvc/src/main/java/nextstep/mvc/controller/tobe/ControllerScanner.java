package nextstep.mvc.controller.tobe;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import nextstep.mvc.controller.tobe.exception.ControllerInstanceCreationFailureException;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.Repository;
import org.reflections.Reflections;

public class ControllerScanner {

    private final Reflections reflections;
    private final Map<Class<?>, Object> container;

    public ControllerScanner(Object... basePackage) {
        reflections = new Reflections(basePackage);
        container = new HashMap<>();
        scanRepositories();
        scanControllers();
    }

    private void scanRepositories() {
        Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(Repository.class);
        for (Class<?> repository : repositories) {
            container.put(repository, instantiate(repository));
        }
    }

    private void scanControllers() {
        Set<Class<?>> preInitiatedControllers = reflections.getTypesAnnotatedWith(Controller.class);
        for (Class<?> controller : preInitiatedControllers) {
            Object controllerInstance = instantiateController(controller);
            container.put(controller, controllerInstance);
        }
    }

    public Map<Class<?>, Object> getControllers() {
        return container.entrySet().stream()
            .filter(entry -> entry.getKey().isAnnotationPresent(Controller.class))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Object instantiateController(Class<?> controllerClass) {
        return Arrays.stream(controllerClass.getDeclaredConstructors())
            .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
            .findFirst()
            .map(this::instantiateWithDependencyInjected)
            .orElseGet(() -> instantiate(controllerClass));
    }

    private Object instantiateWithDependencyInjected(Constructor<?> autowiredControllerConstructor) {
        Object[] objects = Arrays.stream(autowiredControllerConstructor.getParameterTypes())
            .map(type -> container.getOrDefault(type, instantiate(type)))
            .toArray();

        return instantiate(autowiredControllerConstructor, objects);
    }

    private Object instantiate(Class<?> clazz) {
        try {
            return instantiate(clazz.getConstructor());
        } catch (ReflectiveOperationException exception) {
            throw new ControllerInstanceCreationFailureException(exception);
        }
    }

    private Object instantiate(Constructor<?> constructor, Object... parameterInstances) {
        try {
            return constructor.newInstance(parameterInstances);
        } catch (ReflectiveOperationException exception) {
            throw new ControllerInstanceCreationFailureException(exception);
        }
    }
}
