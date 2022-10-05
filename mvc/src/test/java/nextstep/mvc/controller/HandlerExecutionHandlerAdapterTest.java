package nextstep.mvc.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandlerExecutionHandlerAdapterTest {

    private AnnotationHandlerMapping handlerMapping;

    @BeforeEach
    void setUp() {
        handlerMapping = new AnnotationHandlerMapping("samples");
        handlerMapping.initialize();
    }

    @DisplayName("adapter에서 지원하는 handler일 경우 true를 반환한다.")
    @Test
    void supports_true() {
        // given
        final var request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("GET");

        final var annotationHandlerAdapter = new HandlerExecutionHandlerAdapter();
        final var handlerExecution = (HandlerExecution) handlerMapping.getHandler(request);

        // when
        final var actual = annotationHandlerAdapter.supports(handlerExecution);

        // then
        assertThat(actual).isTrue();
    }

    @DisplayName("adapter에서 지원하지 않는 handler일 경우 false를 반환한다.")
    @Test
    void supports_false() {
        // given
        final var annotationHandlerAdapter = new HandlerExecutionHandlerAdapter();
        final var stringController = "Controller";

        // when
        final var actual = annotationHandlerAdapter.supports(stringController);

        // then
        assertThat(actual).isFalse();
    }

    @DisplayName("adapter를 이용해 get 요청을 처리한다.")
    @Test
    void handle_get() {
        // given
        final var annotationHandlerAdapter = new HandlerExecutionHandlerAdapter();

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        when(request.getAttribute("id")).thenReturn("gugu");
        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("GET");

        final var handlerExecution = (HandlerExecution) handlerMapping.getHandler(request);

        // when
        final var modelAndView = annotationHandlerAdapter.handle(request, response, handlerExecution);

        // then
        assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
    }

    @DisplayName("adapter를 이용해 post 요청을 처리한다.")
    @Test
    void handle_post() {
        // given
        final var annotationHandlerAdapter = new HandlerExecutionHandlerAdapter();

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        when(request.getAttribute("id")).thenReturn("gugu");
        when(request.getRequestURI()).thenReturn("/post-test");
        when(request.getMethod()).thenReturn("POST");

        final var handlerExecution = (HandlerExecution) handlerMapping.getHandler(request);

        // when
        final var modelAndView = annotationHandlerAdapter.handle(request, response, handlerExecution);

        // then
        assertThat(modelAndView.getObject("id")).isEqualTo("gugu");
    }
}
