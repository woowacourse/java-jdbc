package reflection;

import annotation.Controller;
import annotation.Repository;
import annotation.Service;
import examples.JdbcQuestionRepository;
import examples.JdbcUserRepository;
import examples.MyQnaService;
import examples.QnaController;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionsTest {

    private static final Logger log = LoggerFactory.getLogger(ReflectionsTest.class);

    @Test
    void showAnnotationClass() throws Exception {
        Reflections reflections = new Reflections("examples");

        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(Controller.class);
        Set<Class<?>> serviceClasses = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> repositoryClasses = reflections.getTypesAnnotatedWith(Repository.class);

        log.info("컨트롤러: {}", controllerClasses);
        log.info("서비스: {}", serviceClasses);
        log.info("레포지토리: {}", repositoryClasses);

        assertThat(controllerClasses).containsExactlyInAnyOrder(QnaController.class);
        assertThat(serviceClasses).containsExactlyInAnyOrder(MyQnaService.class);
        assertThat(repositoryClasses).containsExactlyInAnyOrder(JdbcQuestionRepository.class, JdbcUserRepository.class);
    }
}
