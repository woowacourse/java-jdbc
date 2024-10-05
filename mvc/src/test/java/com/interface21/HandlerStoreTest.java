package com.interface21;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.context.stereotype.Controller;
import com.interface21.webmvc.servlet.mvc.sample.TestController;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandlerStoreTest {

    private HandlerStore handlerStore = HandlerStore.getInstance();

    @BeforeEach
    void setUp() {
        handlerStore.clear();
    }

    @Test
    @DisplayName("Handler를 중복으로 등록할 경우 예외를 발생시킨다")
    void duplicateHandler() {
        HandlerStore handlerStore = HandlerStore.getInstance();
        List<Object> objects = List.of(this, this);

        assertThatThrownBy(() -> handlerStore.registerHandler(objects))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("해당 클래스의 객체를 반환한다")
    void getHandler() {
        HandlerStore handlerStore = HandlerStore.getInstance();
        handlerStore.registerHandler(List.of(this, new TestController()));

        List<TestController> handler = handlerStore.getHandler(TestController.class);

        assertThat(handler).hasSize(1);
    }

    @Test
    @DisplayName("해당 어노테이션이 붙은 객체를 반환한다")
    void getHandlerWithAnnotation() {
        HandlerStore handlerStore = HandlerStore.getInstance();
        handlerStore.registerHandler(List.of(this, new TestController()));

        List<Object> objects = handlerStore.getHandlerWithAnnotation(Controller.class);

        assertThat(objects).hasSize(1);
    }
}
