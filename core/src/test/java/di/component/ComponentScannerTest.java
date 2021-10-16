package di.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class ComponentScannerTest {

    @DisplayName("example 패키지에서 @Component, @Controller, @Service, @Repository @Configuration 가 붙어있는 클래스를 스캔한다")
    @Test
    void scanComponentClasses() {
        ComponentScanner componentScanner = new ComponentScanner("example");
        Set<Class<?>> classes = componentScanner.scanComponentClasses();
        assertThat(classes).hasSize(11);
    }

    @DisplayName("@Configuration 가 붙어있는 클래스 내부 메서드에서 @Component 가 붙어있는 메서드를 찾는다.")
    @Test
    void scanComponentMethodsFromConfiguration() {
        ComponentScanner componentScanner = new ComponentScanner("example");
        Set<Method> methods = componentScanner.scanComponentMethodsFromConfiguration();
        assertThat(methods).hasSize(1);
    }
}
