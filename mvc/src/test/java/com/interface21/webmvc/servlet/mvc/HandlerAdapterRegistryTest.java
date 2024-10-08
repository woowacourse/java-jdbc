package com.interface21.webmvc.servlet.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.webmvc.servlet.mvc.exception.NotSupportedHandlerAdapterException;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerExecution;
import com.interface21.webmvc.servlet.mvc.tobe.HandlerExecutionHandlerAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import samples.SampleController;

public class HandlerAdapterRegistryTest {

    private HandlerAdapterRegistry handlerAdapterRegistry;
    private HandlerAdapter handlerAdapter;

    @BeforeEach
    void setUp() {
        handlerAdapterRegistry = new HandlerAdapterRegistry();
        handlerAdapter = new HandlerExecutionHandlerAdapter();
    }

    @Test
    void 지원하는_HandlerAdapter를_반환한다() {
        // given
        handlerAdapterRegistry.addHandlerAdapter(handlerAdapter);
        Object handler = new HandlerExecution(new SampleController(), SampleController.class.getMethods()[0]);

        // when
        HandlerAdapter result = handlerAdapterRegistry.getHandlerAdapter(handler);

        // then
        assertThat(result).isEqualTo(handlerAdapter);
    }

    @Test
    void 지원하지_않는_Handler일_경우_예외를_던진다() {
        // given
        handlerAdapterRegistry.addHandlerAdapter(handlerAdapter);
        Object nonSupportedHandler = new Object();

        // when & then
        assertThatThrownBy(() -> handlerAdapterRegistry.getHandlerAdapter(nonSupportedHandler))
                .isInstanceOf(NotSupportedHandlerAdapterException.class);
    }
}
