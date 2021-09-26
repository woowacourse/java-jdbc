package reflection;

import annotation.Component;
import annotation.Controller;
import annotation.Repository;
import annotation.Service;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReflectionsTest {

    private static final Logger log = LoggerFactory.getLogger(ReflectionsTest.class);

    @Test
    void showAnnotationClass() throws Exception {
        Reflections reflections = new Reflections("examples");

        Set<Class<?>> typesAnnotatedWithComponent = reflections.getTypesAnnotatedWith(Component.class);
        Set<Class<?>> typesAnnotatedWithRepository = reflections.getTypesAnnotatedWith(Repository.class);
        Set<Class<?>> typesAnnotatedWithController = reflections.getTypesAnnotatedWith(Controller.class);
        Set<Class<?>> typesAnnotatedWithService = reflections.getTypesAnnotatedWith(Service.class);

        logAnnotation(typesAnnotatedWithComponent);
        logAnnotation(typesAnnotatedWithRepository);
        logAnnotation(typesAnnotatedWithController);
        logAnnotation(typesAnnotatedWithService);
    }

    private void logAnnotation(final Set<Class<?>> annotatedClass) {
        for (Class clazz : annotatedClass) {
            log.info("Annotated Class Name : {}", clazz.getSimpleName());
        }
    }
}
