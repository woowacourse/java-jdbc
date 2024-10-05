package com.interface21.webmvc.servlet.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.ContextLoaderTest;
import com.interface21.HandlerContainer;
import com.interface21.webmvc.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandlerAdaptersTest {

    static class DummyHandlerAdapter implements HandlerAdapter {

        @Override
        public boolean supports(Object handler) {
            return handler instanceof String && handler.equals("test");
        }

        @Override
        public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
                throws Exception {
            ModelAndView mv = mock(ModelAndView.class);
            when(mv.getObject("test")).thenReturn("test success");
            return mv;
        }
    }

    @BeforeEach
    void setUp() {
        HandlerContainer instance = HandlerContainer.getInstance();
        instance.clear();
        instance.initialize(ContextLoaderTest.class);
    }

    @DisplayName("HandlerAdapter를 구현하는 클래스를 저장한 뒤 적절한 Adapter를 활용해 들어온 요청을 처리한다")
    @Test
    void handle() throws Exception {
        HandlerAdapters handlerAdapters = new HandlerAdapters();
        handlerAdapters.initialize();

        ModelAndView modelAndView = handlerAdapters.handle(null, null, "test");

        assertThat(modelAndView.getObject("test")).isEqualTo("test success");
    }

    @DisplayName("HandlerAdapter를 구현하는 클래스를 저장한 뒤 적절한 Adapter를 활용해 들어온 요청을 처리한다")
    @Test
    void notExistMatchHandlerAdapter() {
        HandlerAdapters handlerAdapters = new HandlerAdapters();
        handlerAdapters.initialize();

        assertThatThrownBy(() -> handlerAdapters.handle(null, null, "not exist"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
