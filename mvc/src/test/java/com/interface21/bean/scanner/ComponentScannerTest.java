package com.interface21.bean.scanner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import samples.TestController;

class ComponentScannerTest {

    @DisplayName("@Component가 메타 어노테이션을 포함하여 존재하는 Class를 스캔한다.")
    @Test
    void componentScan() {
        List<Class<?>> classes = ComponentScanner.componentScan(TestController.class.getPackageName());

        assertThat(classes).hasSize(7)
                .extracting(Class::getSimpleName)
                .containsExactlyInAnyOrder("TestController", "TestController1", "TestController2",
                        "TestFailHandlerMappings", "TestSuccessHandlerMappings",
                        "TestSuccessHandlerAdapter", "TestFailHandlerAdapter");
    }
}
