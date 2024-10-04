package com.interface21.bean.container;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.context.stereotype.Controller;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import samples.TestController1;
import samples.TestController2;
import samples.TestController3;

class BeanContainerTest {

    BeanContainer beanContainer = BeanContainer.getInstance();

    @BeforeEach
    void setUp() {
        beanContainer.clear();
    }

    @DisplayName("존재하지 않는 빈은 찾을 수 없다.")
    @Test
    void notExistBean() {
        assertThatThrownBy(() -> beanContainer.getBean(BeanContainerTest.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("특정 애너테이션이 포함된 빈을 찾을 수 있다.")
    @Test
    void getAnnotatedBeans() {
        beanContainer.registerBeans(List.of(new TestController1(), new TestController2(), new TestController3()));
        List<Object> annotatedBeans = beanContainer.getAnnotatedBeans(Controller.class);

        assertThat(annotatedBeans).hasSize(2)
                .extracting(bean -> bean.getClass().getSimpleName())
                .containsExactlyInAnyOrder("TestController1", "TestController2");
    }

    @DisplayName("특정 클래스를 구현한 빈을 찾을 수 있다.")
    @Test
    void getSubTypeBeansOf() {
        beanContainer.registerBeans(List.of(new TestController1(), new TestController2(), new TestController3()));
        List<TestController2> annotatedBeans = beanContainer.getSubTypeBeansOf(TestController2.class);

        assertThat(annotatedBeans).hasSize(2)
                .extracting(bean -> bean.getClass().getSimpleName())
                .containsExactlyInAnyOrder("TestController2", "TestController3");
    }

    @DisplayName("컨테이너는 객체를 싱글톤으로 관리한다.")
    @Test
    void singleTon() {
        beanContainer.registerBeans(List.of(new BeanContainerTest()));
        Object bean1 = beanContainer.getBean(BeanContainerTest.class);
        Object bean2 = beanContainer.getBean(BeanContainerTest.class);

        assertThat(bean1).isEqualTo(bean2);
    }
}
