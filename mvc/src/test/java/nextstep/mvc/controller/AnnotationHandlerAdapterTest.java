package nextstep.mvc.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.controller.AnnotationHandlerAdapter;
import nextstep.mvc.controller.HandlerExecution;
import nextstep.mvc.view.ModelAndView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AnnotationHandlerAdapter는")
class AnnotationHandlerAdapterTest {

    private AnnotationHandlerAdapter annotationHandlerAdapter;

    @BeforeEach
    void setUp() {
        annotationHandlerAdapter = new AnnotationHandlerAdapter();
    }

    @DisplayName("handler 지원 여부를 물었을 때")
    @Nested
    class Supports {

        @DisplayName("handler가 HandlerExecution 구현체인 경우 true를 반환한다.")
        @Test
        void supportsTrue() {
            // given
            Object handler = mock(HandlerExecution.class);

            // when, then
            assertThat(annotationHandlerAdapter.isCompatible(handler)).isTrue();
        }

        @DisplayName("handler가 HandlerExecution 구현체가 아닌 경우 false를 반환한다.")
        @Test
        void supportsFalse() {
            // given
            Object handler = mock(Object.class);

            // when, then
            assertThat(annotationHandlerAdapter.isCompatible(handler)).isFalse();
        }
    }

    @DisplayName("handle 요청을 수행했을 때")
    @Nested
    class Handle {

        private HttpServletRequest request;
        private HttpServletResponse response;

        @BeforeEach
        void setUp() {
            request = mock(HttpServletRequest.class);
            response = mock(HttpServletResponse.class);
        }

        @DisplayName("HandlerExecution 구현체를 handler로 전달할 경우 ModelAndView를 반환한다.")
        @Test
        void handleReturnModelAndView() throws Exception {
            // given
            HandlerExecution handler = mock(HandlerExecution.class);
            ModelAndView modelAndView = mock(ModelAndView.class);

            when(handler.handle(request, response)).thenReturn(modelAndView);

            // when, then
            assertThat(annotationHandlerAdapter.handle(request, response, handler))
                .isInstanceOf(ModelAndView.class);
        }

        @DisplayName("HandlerExecution 구현체가 아닌 handler를 전달할 경우 예외가 발생한다.")
        @Test
        void handleException() {
            // given
            Object handler = mock(Object.class);

            // when, then
            assertThatThrownBy(() -> annotationHandlerAdapter.handle(request, response, handler))
                .isExactlyInstanceOf(ClassCastException.class);
        }
    }
}