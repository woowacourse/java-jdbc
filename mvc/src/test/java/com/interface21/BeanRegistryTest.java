package com.interface21;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.context.stereotype.Controller;
import com.interface21.webmvc.servlet.mvc.sample.TestController;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BeanRegistryTest {

    private BeanRegistry beanRegistry = BeanRegistry.getInstance();

    @BeforeEach
    void setUp() {
        beanRegistry.clear();
    }

    @Test
    @DisplayName("Handler를 중복으로 등록할 경우 예외를 발생시킨다")
    void duplicateHandler() {
        BeanRegistry beanRegistry = BeanRegistry.getInstance();
        Set<Object> objects = Set.of(this);
        beanRegistry.registerBeans(objects);

        assertThatThrownBy(() -> beanRegistry.registerBeans(objects))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("해당 클래스의 객체를 반환한다")
    void getHandler() {
        BeanRegistry beanRegistry = BeanRegistry.getInstance();
        beanRegistry.registerBeans(Set.of(this, new TestController()));

        List<TestController> handler = beanRegistry.getBeans(TestController.class);

        assertThat(handler).hasSize(1);
    }

    @Test
    @DisplayName("해당 어노테이션이 붙은 객체를 반환한다")
    void getHandlerWithAnnotation() {
        BeanRegistry beanRegistry = BeanRegistry.getInstance();
        beanRegistry.registerBeans(Set.of(this, new TestController()));

        List<Object> objects = beanRegistry.getBeansWithAnnotation(Controller.class);

        assertThat(objects).hasSize(1);
    }
}
