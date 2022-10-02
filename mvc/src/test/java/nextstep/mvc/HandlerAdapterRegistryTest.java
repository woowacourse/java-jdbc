package nextstep.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.controller.AnnotationHandlerMapping;
import nextstep.mvc.controller.HandlerExecutionHandlerAdapter;
import nextstep.mvc.controller.exception.NotSupportHandler;
import nextstep.mvc.view.ModelAndView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandlerAdapterRegistryTest {

    @DisplayName("핸들러 어댑터를 추가하고 핸들러를 통해 적절히 요청을 처리할 수 있다.")
    @Test
    void addAndGetHandlerAdapter() throws Exception {
        // given
        final HandlerAdapterRegistry handlerAdapterRegistry = new HandlerAdapterRegistry();
        handlerAdapterRegistry.addHandlerAdapter(new HandlerExecutionHandlerAdapter());

        // when
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        when(request.getAttribute("id")).thenReturn("dwoo");
        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("GET");

        final AnnotationHandlerMapping handlerMapping = new AnnotationHandlerMapping("samples");
        handlerMapping.initialize();
        final Object handler = handlerMapping.getHandler(request);

        final HandlerAdapter handlerAdapter = handlerAdapterRegistry.getHandlerAdapter(handler);

        // then
        final ModelAndView modelAndView = handlerAdapter.handle(request, response, handler);
        assertThat(modelAndView.getObject("id")).isEqualTo("dwoo");
    }

    @DisplayName("핸들러 어댑터를 찾을 수 없는 경우 예외를 발생한다.")
    @Test
    void notFoundHandlerAdapter() {
        // given
        final HandlerAdapterRegistry handlerAdapterRegistry = new HandlerAdapterRegistry();

        // when
        final var request = mock(HttpServletRequest.class);

        when(request.getAttribute("id")).thenReturn("dwoo");
        when(request.getRequestURI()).thenReturn("/get-test");
        when(request.getMethod()).thenReturn("GET");

        final AnnotationHandlerMapping handlerMapping = new AnnotationHandlerMapping("samples");
        handlerMapping.initialize();
        final Object handler = handlerMapping.getHandler(request);

        // then
        assertThatThrownBy(() -> handlerAdapterRegistry.getHandlerAdapter(handler))
                .isInstanceOf(NotSupportHandler.class);
    }
}
