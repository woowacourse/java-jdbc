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

class ReflectionsTest extends OutputTest {

    @Test
    void showAnnotationClass() {
        // given
        Reflections reflections = new Reflections("examples");

        // when
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        for (Class<?> controller : controllers) {
            System.out.println(controller.getSimpleName());
        }

        Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
        for (Class<?> service : services) {
            System.out.println(service.getSimpleName());
        }

        Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(Repository.class);
        for (Class<?> repository : repositories) {
            System.out.println(repository.getSimpleName());
        }

        // then
        String output = captor.toString().trim();
        assertThat(output).contains(
                        "QnaController",
                        "MyQnaService",
                        "JdbcQuestionRepository",
                        "JdbcUserRepository"
        );
    }
}
