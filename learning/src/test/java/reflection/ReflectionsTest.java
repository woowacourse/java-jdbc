package reflection;

import static org.assertj.core.api.Assertions.assertThat;

import annotation.Controller;
import annotation.Repository;
import annotation.Service;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;


class ReflectionsTest {

    private static final Logger log = (Logger) LoggerFactory.getLogger(ReflectionsTest.class);
    private static final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    static {
        listAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        log.setLevel(Level.DEBUG);
        log.addAppender(listAppender);
        listAppender.start();
    }

    @Test
    void showAnnotationClass() {
        Reflections reflections = new Reflections("examples");
        List<Class<? extends Annotation>> annotations = List.of(Controller.class, Service.class, Repository.class);

        for (Class<? extends Annotation> annotation : annotations) {
            Set<Class<?>> types = reflections.getTypesAnnotatedWith(annotation);

            for (Class<?> type : types) {
                log.debug(type.getName());
            }
        }

        Set<String> result = listAppender.list.stream().map(Object::toString).collect(Collectors.toSet());

        assertThat(result).contains("[DEBUG] examples.QnaController", "[DEBUG] examples.MyQnaService", "[DEBUG] examples.JdbcUserRepository",
            "[DEBUG] examples.JdbcQuestionRepository");
    }
}
