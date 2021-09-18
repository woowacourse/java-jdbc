package reflection;

import annotation.Controller;
import annotation.Repository;
import annotation.Service;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReflectionsTest {

    private static final Logger log = LoggerFactory.getLogger(ReflectionsTest.class);

    @Test
    void showAnnotationClass() throws Exception {
        Reflections reflections = new Reflections("examples");
        // TODO 클래스 레벨에 @Controller, @Service, @Repository 애노테이션이 설정되어 모든 클래스 찾아 로그로 출력한다.
        Set<Class<?>> typesAnnotatedWithController = reflections.getTypesAnnotatedWith(Controller.class);
        Set<Class<?>> typesAnnotatedWithService = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> typesAnnotatedWithRepository = reflections.getTypesAnnotatedWith(Repository.class);

        log.info("Controller 클래스");
        typesAnnotatedWithController.stream()
            .map(Class::getSimpleName)
            .forEach(log::info);
        log.info("===============");

        log.info("Service 클래스");
        typesAnnotatedWithService.stream()
            .map(Class::getSimpleName)
            .forEach(log::info);
        log.info("===============");

        log.info("Repository 클래스");
        typesAnnotatedWithRepository.stream()
            .map(Class::getSimpleName)
            .forEach(log::info);
        log.info("===============");
    }
}
