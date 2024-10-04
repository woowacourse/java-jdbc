package com.interface21.bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.bean.container.BeanContainer;
import com.interface21.context.stereotype.Component;
import com.interface21.webmvc.servlet.mvc.HandlerAdapter;
import com.interface21.webmvc.servlet.mvc.HandlerMapping;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import samples.TestController;

class BeanRegisterTest {

    @BeforeEach
    void setUp() {
        BeanContainer beanContainer = BeanContainer.getInstance();
        beanContainer.clear();
    }

    @DisplayName("특정 클래스 패키지 기준으로 @Component가 붙은 class와 HandlerAdapter, HandlerMapping 구현체들을 빈 컨테이너에 등록한다.")
    @Test
    void run() {
        BeanRegister.run(TestController.class);
        BeanContainer beanContainer = BeanContainer.getInstance();

        List<Object> annotatedBeans = beanContainer.getAnnotatedBeans(Component.class);
        List<HandlerMapping> handlerMappings = beanContainer.getSubTypeBeansOf(HandlerMapping.class);
        List<HandlerAdapter> handlerAdapters = beanContainer.getSubTypeBeansOf(HandlerAdapter.class);

        assertAll(
                () -> assertThat(annotatedBeans).hasSize(4)
                        .extracting(bean -> bean.getClass().getSimpleName())
                        .containsExactlyInAnyOrder("TestFailHandlerMappings", "TestSuccessHandlerMappings",
                                "TestFailHandlerAdapter", "TestSuccessHandlerAdapter"),
                () -> assertThat(handlerMappings).hasSize(3)
                        .extracting(bean -> bean.getClass().getSimpleName())
                        .containsExactlyInAnyOrder(
                                "TestFailHandlerMappings", "TestSuccessHandlerMappings", "AnnotationHandlerMapping"),
                () -> assertThat(handlerAdapters).hasSize(4)
                        .extracting(bean -> bean.getClass().getSimpleName())
                        .containsExactlyInAnyOrder("HandlerExecutionHandlerAdapter", "ControllerHandlerAdapter",
                                "TestFailHandlerAdapter", "TestSuccessHandlerAdapter")
        );
    }
}
