package com.interface21.bean.scanner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import samples.TestController1;

class SubTypeScannerTest {

    @DisplayName("특정 클래스의 구현체들을 스캔한다.")
    @Test
    void componentScan() {
        Set<Class<? extends TestController1>> classes = SubTypeScanner.subTypeScan(TestController1.class, TestController1.class.getPackageName());

        assertThat(classes).hasSize(2)
                .extracting(Class::getSimpleName)
                .containsExactlyInAnyOrder("TestController2", "TestController3");
    }
}
