package di.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentScannerTest {

    @DisplayName("example 패키지에서 @Component, @Controller, @Service, @Repository 가 붙어있는 클래스를 찾는다.")
    @Test
    void scanComponentClasses() {
        ComponentScanner componentScanner = new ComponentScanner("example");
        Set<Class<?>> classes = componentScanner.scanComponentClasses();
        assertThat(classes).hasSize(10);
    }

}
