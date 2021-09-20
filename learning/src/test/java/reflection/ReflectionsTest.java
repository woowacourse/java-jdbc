package reflection;

import annotation.Controller;
import annotation.Repository;
import annotation.Service;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionsTest {

    private static final Logger log = LoggerFactory.getLogger(ReflectionsTest.class);

    @Test
    void showAnnotationClass() {
        Reflections reflections = new Reflections("examples");

        // TODO 클래스 레벨에 @Controller, @Service, @Repository 애노테이션이 설정되어 모든 클래스 찾아 로그로 출력한다.
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        log.info("Controller : {}", controllers);
        int expectedControllersSize = 1;
        assertThat(controllers).hasSize(expectedControllersSize);

        Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
        log.info("Service : {}", services);
        int expectedServicesSize = 1;
        assertThat(services).hasSize(expectedServicesSize);

        Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(Repository.class);
        log.info("Repository : {}", repositories);
        int expectedRepositoriesSize = 2;
        assertThat(repositories).hasSize(expectedRepositoriesSize);
    }
}
