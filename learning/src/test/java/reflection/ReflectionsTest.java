package reflection;

import annotation.Controller;
import annotation.Repository;
import annotation.Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReflectionsTest {

    private static final Logger log = LoggerFactory.getLogger(ReflectionsTest.class);

    @DisplayName("클래스 레벨에 @Controller, @Service, @Repository 애노테이션이 설정되어 모든 클래스 찾아 로그로 출력한다.")
    @Test
    void showAnnotationClass() throws Exception {
        Reflections reflections = new Reflections("examples");

        reflections.getTypesAnnotatedWith(Controller.class)
                .forEach(controllerClass -> log.info(Controller.class.getSimpleName() + " : " + controllerClass.getName()));
        reflections.getTypesAnnotatedWith(Service.class)
                .forEach(serviceClass -> log.info(Service.class.getSimpleName() + " : " + serviceClass.getName()));
        reflections.getTypesAnnotatedWith(Repository.class)
                .forEach(repositoryClass -> log.info(Repository.class.getSimpleName() + " : " + repositoryClass.getName()));
    }
}
