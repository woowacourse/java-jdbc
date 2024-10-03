package com.interface21.webmvc.servlet.mvc.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.webmvc.servlet.mvc.tobe.HandlerExecution;
import com.interface21.webmvc.servlet.view.ModelAndView;
import com.interface21.webmvc.servlet.view.View;
import com.interface21.webmvc.servlet.view.ViewResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnnotationMethodHandlerAdapterTest {

    @DisplayName("handlerExecution의 handle 반환타입이 ModelAndView이면 그대로 반환한다.")
    @Test
    void handleModelAndViewType() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var viewResolver = mock(ViewResolver.class);
        final var handlerExecution = mock(HandlerExecution.class);

        ModelAndView modelAndView = new ModelAndView(mock(View.class));
        when(handlerExecution.handle(request, response)).thenReturn(modelAndView);

        AnnotationMethodHandlerAdapter annotationMethodHandlerAdapter = new AnnotationMethodHandlerAdapter(
                viewResolver);

        ModelAndView result = annotationMethodHandlerAdapter.handle(handlerExecution, request, response);

        assertThat(result).isInstanceOf(ModelAndView.class);
    }

    @DisplayName("handlerExecution의 handle 반환타입이 String이면 ModelAndView로 변환해 반환한다.")
    @Test
    void handleStringType() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var viewResolver = mock(ViewResolver.class);
        final var handlerExecution = mock(HandlerExecution.class);

        String viewName = "home";
        when(handlerExecution.handle(request, response)).thenReturn(viewName);
        when(viewResolver.resolveViewName(viewName)).thenReturn(mock(View.class));

        AnnotationMethodHandlerAdapter annotationMethodHandlerAdapter = new AnnotationMethodHandlerAdapter(
                viewResolver);

        ModelAndView result = annotationMethodHandlerAdapter.handle(handlerExecution, request, response);

        assertThat(result).isInstanceOf(ModelAndView.class);
    }

    @DisplayName("handlerExecution의 handle 반환타입이 View이면 ModelAndView로 변환해 반환한다.")
    @Test
    void handleViewType() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var viewResolver = mock(ViewResolver.class);
        final var handlerExecution = mock(HandlerExecution.class);

        View view = mock(View.class);
        when(handlerExecution.handle(request, response)).thenReturn(view);

        AnnotationMethodHandlerAdapter annotationMethodHandlerAdapter = new AnnotationMethodHandlerAdapter(
                viewResolver);

        ModelAndView result = annotationMethodHandlerAdapter.handle(handlerExecution, request, response);

        assertThat(result).isInstanceOf(ModelAndView.class);
    }
}
