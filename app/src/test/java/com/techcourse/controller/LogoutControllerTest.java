package com.techcourse.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.mvc.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogoutControllerTest {

    @DisplayName("로그아웃 처리를 할 수 있다.")
    @Test
    void logout() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var session = mock(HttpSession.class);
        final LogoutController logoutController = new LogoutController();

        when(request.getRequestURI()).thenReturn("/logout");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession()).thenReturn(session);

        final ModelAndView modelAndView = logoutController.logout(request, response);
        final View view = modelAndView.getView();

        modelAndView.renderView(request, response);

        assertThat(view).isInstanceOf(JspView.class);
        verify(response).sendRedirect("/");
    }
}
