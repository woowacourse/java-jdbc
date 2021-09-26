package nextstep.mvc.controller.tobe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nextstep.web.annotation.Controller;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerScanner {

    private static final Logger log = LoggerFactory.getLogger(ControllerScanner.class);

    private final Reflections reflections;

    public ControllerScanner(Object... basePackage) {
        reflections = new Reflections(basePackage);
    }

    public Map<Class<?>, Object> getControllers() {
        Set<Class<?>> preInitiatedControllers = reflections.getTypesAnnotatedWith(Controller.class);
        ServiceScanner serviceScanner = new ServiceScanner(reflections);
        Map<Class<?>, Object> services = serviceScanner.getServices();
        return instantiateControllers(preInitiatedControllers, services);
    }

    Map<Class<?>, Object> instantiateControllers(Set<Class<?>> preInitiatedControllers,
        Map<Class<?>, Object> services) {
        final Map<Class<?>, Object> controllers = new HashMap<>();
        try {
            for (Class<?> clazz : preInitiatedControllers) {
                fillControllerRequirement(services, controllers, clazz);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error(e.getMessage());
        }

        return controllers;
    }

    private void fillControllerRequirement(Map<Class<?>, Object> services,
        Map<Class<?>, Object> controllers, Class<?> clazz)
        throws InstantiationException, IllegalAccessException, InvocationTargetException {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            Object[] requiredParameters = constructor.getParameterTypes();
            List<Object> pairedParameters = new LinkedList<>();

            fillControllerConstructorRequirement(services, requiredParameters, pairedParameters);
            controllers.put(clazz, constructor.newInstance(pairedParameters.toArray()));
        }
    }

    private void fillControllerConstructorRequirement(Map<Class<?>, Object> services, Object[] requiredParameters,
        List<Object> pairedParameters) {
        for (Object requiredParameter : requiredParameters) {
            Object matchedParameter = services.get(requiredParameter);
            pairedParameters.add(matchedParameter);
        }
    }

}
