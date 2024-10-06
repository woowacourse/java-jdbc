package com.interface21;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.context.stereotype.Controller;
import com.interface21.webmvc.servlet.mvc.sample.TestController;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BeanScannerTest {

    @Test
    @DisplayName("해당 어노테이션이 붙은 클래스를 찾아온다")
    void scanTypesAnnotatedWith() {
        // 현재 test 패키지에 위치한 클래스들 중 Controller 어노테이션을 상속받은 클래스 탐색 (TestController)
        List<Object> objects = BeanScanner.scanTypesAnnotatedWith(ContextLoaderTest.class, Controller.class);

        assertAll(
                () -> assertThat(objects).hasSize(1),
                () -> assertThat(objects.get(0)).isInstanceOf(TestController.class)
        );
    }
}
