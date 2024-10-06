package com.interface21.webmvc.servlet.mvc.tobe;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import samples.TestController;

public class ControllerScannerTest {

    private static final Logger log = LoggerFactory.getLogger(ControllerScannerTest.class);

    private ControllerScanner controllerScanner;

    @BeforeEach
    void setUp() {
        Object[] basePackage = new String[]{"samples"};
        controllerScanner = new ControllerScanner(basePackage);
    }

    @Test
    void 클래스_타입으로_클래스_인스턴스를_가져올_수_있다() {
        // given
        Class<?> clazz = TestController.class;

        // when
        Map<Class<?>, Object> controllers = controllerScanner.getControllers();

        // then
        assertThat(controllers.get(clazz)).isInstanceOf(TestController.class);
    }

    @Test
    void 메서드의_정보로_메서드가_속한_클래스_인스턴스를_가져올_수_있다() throws NoSuchMethodException {
        // given
        Method method = TestController.class.getMethod(
                "findUserId", HttpServletRequest.class, HttpServletResponse.class);
        Class<?> clazz = method.getDeclaringClass();

        // when
        Map<Class<?>, Object> controllers = controllerScanner.getControllers();

        // then
        assertThat(controllers.get(clazz)).isInstanceOf(TestController.class);
    }
}
