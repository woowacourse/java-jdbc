package reflection;

import annotation.Controller;
import annotation.Repository;
import annotation.Service;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionsTest extends OutputTest {

    @Test
    void showAnnotationClass() {
        // given
        Reflections reflections = new Reflections("examples");

        // when
        printClasses(reflections, Controller.class);
        printClasses(reflections, Service.class);
        printClasses(reflections, Repository.class);

        // then
        String output = captor.toString().trim();
        assertThat(output).contains(
                        "QnaController",
                        "MyQnaService",
                        "JdbcQuestionRepository",
                        "JdbcUserRepository"
        );
    }

    private <T extends Annotation> void printClasses(Reflections reflections, Class<T> aClass) {
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(aClass);
        for (Class<?> clazz : classes) {
            System.out.println(clazz.getSimpleName());
        }
    }
}
