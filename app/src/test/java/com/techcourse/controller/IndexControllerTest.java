package com.techcourse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.mvc.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IndexControllerTest {

    @DisplayName("루트로 요청시 index.jsp 를 보여줄 수 있다.")
    @Test
    void renderIndexView() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        final IndexController indexController = new IndexController();

        when(request.getRequestDispatcher("/index.jsp")).thenReturn(requestDispatcher);
        when(request.getRequestURI()).thenReturn("/");
        when(request.getMethod()).thenReturn("GET");

        final ModelAndView modelAndView = indexController.renderMainView(request, response);
        final View view = modelAndView.getView();

        modelAndView.renderView(request, response);

        assertThat(view).isInstanceOf(JspView.class);
        verify(requestDispatcher).forward(request, response);
    }
}
