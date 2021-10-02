package nextstep.mvc.controller.tobe;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import nextstep.mvc.controller.tobe.exception.ControllerInstanceCreationFailureException;
import nextstep.web.annotation.Autowired;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.Repository;
import org.reflections.Reflections;

public class ControllerScanner {

    private final Reflections reflections;

    public ControllerScanner(Object... basePackage) {
        reflections = new Reflections(basePackage);
    }

    public Map<Class<?>, Object> getControllers() {
        Set<Class<?>> preInitiatedControllers = reflections.getTypesAnnotatedWith(Controller.class);
        return instantiateControllers(preInitiatedControllers);
    }

    private Map<Class<?>, Object> instantiateControllers(Set<Class<?>> preInitiatedControllers) {
        Map<Class<?>, Object> controllers = new HashMap<>();

        for (Class<?> controller : preInitiatedControllers) {
            registerController(controller, controllers);
        }

        return controllers;
    }

    private void registerController(Class<?> controller, Map<Class<?>, Object> controllers) {
        Object controllerInstance = instantiateController(controller);
        controllers.put(controller, controllerInstance);
    }

    private Object instantiateController(Class<?> controllerClass) {
        Constructor<?> autowiredControllerConstructor = scanAutowiredConstructor(controllerClass);
        if (autowiredControllerConstructor == null) {
            return instantiate(controllerClass);
        }
        return instantiateDependencyInjectedController(autowiredControllerConstructor);
    }

    private Constructor<?> scanAutowiredConstructor(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredConstructors())
            .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
            .findFirst()
            .orElse(null);
    }

    private Object instantiate(Constructor<?> constructor, Object... parameterInstances) {
        try {
            return constructor.newInstance(parameterInstances);
        } catch (ReflectiveOperationException exception) {
            throw new ControllerInstanceCreationFailureException(exception);
        }
    }

    private Object instantiate(Class<?> clazz) {
        try {
            return instantiate(clazz.getConstructor());
        } catch (ReflectiveOperationException exception) {
            throw new ControllerInstanceCreationFailureException(exception);
        }
    }

    private Object instantiateDependencyInjectedController(Constructor<?> autowiredControllerConstructor) {
        List<Class<?>> repositoryParameterClasses = scanParameters(autowiredControllerConstructor, Repository.class);
        List<Object> repositoryInstances = createInstances(repositoryParameterClasses);
        return instantiate(autowiredControllerConstructor, repositoryInstances.toArray());
    }

    private <T extends Annotation> List<Class<?>> scanParameters(Constructor<?> autowiredConstructors, Class<T> clazz) {
        Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(clazz);
        return Arrays.stream(autowiredConstructors.getParameters())
            .map(Parameter::getType)
            .filter(repositories::contains)
            .collect(Collectors.toList());
    }

    private List<Object> createInstances(List<Class<?>> parameterClasses) {
        List<Object> instances = new ArrayList<>();
        for (Class<?> repositoryParameterClass : parameterClasses) {
            Object repositoryInstance = instantiate(repositoryParameterClass);
            instances.add(repositoryInstance);
        }
        return instances;
    }
}
