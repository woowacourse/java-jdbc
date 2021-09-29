package reflection;

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
        Set<Class<?>> annotatedController = reflections.getTypesAnnotatedWith(Controller.class);
        Set<Class<?>> annotatedService = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> annotatedRepository = reflections.getTypesAnnotatedWith(Repository.class);

        log.info("Annotated Controller : {}", annotatedController.toString());
        log.info("Annotated Service : {}", annotatedService.toString());
        log.info("Annotated Repository : {}", annotatedRepository.toString());
    }
}
