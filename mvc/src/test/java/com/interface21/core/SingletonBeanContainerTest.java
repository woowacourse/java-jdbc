package com.interface21.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SingletonBeanContainerTest {

    static class Dummy {
        public Dummy() {
        }
    }

    static class PrivateDummy {
        private PrivateDummy() {
        }
    }

    @Test
    @DisplayName("여러 번 요청하더라도 같은 인스턴스를 반환한다.")
    void identicalInstanceOnMultipleRequest() {
        SingletonBeanContainer manager = SingletonBeanContainer.getInstance();
        Object dummy1 = manager.registerBean(Dummy.class);
        Object dummy2 = manager.registerBean(Dummy.class);
        assertThat(dummy1).isSameAs(dummy2);
    }

    @Test
    @DisplayName("객체 생성에 실패하는 경우 예외가 발생한다.")
    void privateConstructor() {
        SingletonBeanContainer manager = SingletonBeanContainer.getInstance();
        assertThatThrownBy(() -> manager.getBean(PrivateDummy.class))
                .isInstanceOf(BeanNotFoundException.class);
    }
}
