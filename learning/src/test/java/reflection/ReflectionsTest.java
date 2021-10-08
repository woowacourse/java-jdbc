package reflection;

import annotation.Component;
import annotation.Configuration;
import annotation.Controller;
import annotation.Inject;
import annotation.Repository;
import annotation.Service;
import com.google.common.base.Predicate;
import examples.CustomComponent;
import examples.DummyDataSource;
import examples.JdbcQuestionRepository;
import examples.JdbcUserRepository;
import examples.MyQnaService;
import examples.Parent;
import examples.QnaController;
import examples.TestConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionsTest {

    private static final Logger log = LoggerFactory.getLogger(ReflectionsTest.class);

    @DisplayName("클래스 레벨에 @Controller, @Service, @Repository 애노테이션이 설정되어 모든 클래스 찾아 로그로 출력한다.")
    @Test
    void showAnnotationClass() throws Exception {
        Reflections reflections = new Reflections("examples");

        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
        Set<Class<?>> repositories = reflections.getTypesAnnotatedWith(Repository.class);

        log.debug("Controllers: {}", controllers);
        log.debug("Services: {}", services);
        log.debug("Repositories: {}", repositories);
    }

    @DisplayName("@Component 가 붙어있는 어노테이션을 찾는다.")
    @Test
    void findComponentAnnotations() {
        Reflections reflections = new Reflections("annotation", "example");

        List<Class<?>> annotations = reflections.getTypesAnnotatedWith(Component.class)
                .stream()
                .filter(Class::isAnnotation)
                .collect(Collectors.toList());

        assertThat(annotations).hasSize(4);
    }

    @DisplayName("@Component가 붙어있는 어노테이션들을 가지는 클래스를 찾는다.")
    @Test
    void findComponentClasses() {
        Reflections reflections = new Reflections("annotation", "example");

        Set<Class<?>> componentTypes = reflections.getTypesAnnotatedWith(Component.class);

        Set<Class<?>> annotations = componentTypes.stream()
                .filter(Class::isAnnotation)
                .collect(Collectors.toSet());

        annotations.add(Component.class);

        assertThat(annotations).contains(Component.class, Service.class, Repository.class, Controller.class);


        Set<Class<?>> components = new HashSet<>();
        for (Class<?> annotation : annotations) {
            final Class<Annotation> castedAnnotation = (Class<Annotation>) annotation;
            components.addAll(
                    reflections.getTypesAnnotatedWith(castedAnnotation)
                            .stream()
                            .filter(type -> !type.isInterface() && !type.isAnnotation()) // 클래스인지 검사하는 방법을 못 찾아서..
                            .collect(Collectors.toSet())
            );
        }
        assertThat(components).contains(
                CustomComponent.class,
                JdbcQuestionRepository.class,
                JdbcUserRepository.class,
                MyQnaService.class,
                QnaController.class
        );
    }

    @DisplayName("@Inject 가 붙어있는 생성자를 찾는다.")
    @Test
    void findInjectConstructor() {
        Class<?> aClass = MyQnaService.class;
        Predicate<AnnotatedElement> predicate = ReflectionUtils.withAnnotation(Inject.class);
        Set<Constructor> constructors = ReflectionUtils.getConstructors(aClass, predicate);
        assertThat(constructors).hasSize(1);
    }

    @DisplayName("생성자를 정의하지 않으면 Default 생성자가 존재한다.")
    @Test
    void checkDefaultConstructor() {
        Class<?> aClass = Parent.class;
        final Constructor<?>[] constructors = aClass.getConstructors();
        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getParameterCount()).isZero();
    }

    @DisplayName("@Inject 가 붙어있는 필드를 찾는다.")
    @Test
    void findInjectField() {
        Class<Parent> aClass = Parent.class;
        Set<Field> fields = ReflectionUtils.getFields(aClass, ReflectionUtils.withAnnotation(Inject.class));
        assertThat(fields).hasSize(1);
    }

    @DisplayName("@Configuration 이 붙어있는 클래스의 내부에 @Component 가 붙어있는 메서드를 찾아 객체를 생성한다.")
    @Test
    void findConfigurationAndComponentMethod() throws ReflectiveOperationException {
        Reflections reflections = new Reflections("examples");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Configuration.class);
        Class<?> aClass = classes.stream().findAny().get();
        Constructor<?>[] constructors = aClass.getConstructors();
        TestConfiguration testConfiguration = (TestConfiguration) constructors[0].newInstance();

        Set<Method> methods = ReflectionUtils.getMethods(aClass, method -> method.isAnnotationPresent(Component.class));

        List<Object> list = new ArrayList<>();
        for (Method method : methods) {
            list.add(method.invoke(testConfiguration));
        }

        assertThat(list).hasSize(1);
        assertThat(list.get(0)).isInstanceOf(DummyDataSource.class);
    }
}
