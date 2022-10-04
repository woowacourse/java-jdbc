package com.techcourse.controller;

import static com.techcourse.controller.UserSession.SESSION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techcourse.domain.User;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.mvc.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoginControllerTest {

    @DisplayName("로그인 요청시 로그인을 처리하고 index.jsp 로 리다이렉트한다.")
    @Test
    void login() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var session = mock(HttpSession.class);
        final LoginController loginController = new LoginController();

        when(request.getParameter("account")).thenReturn("gugu");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession()).thenReturn(session);
        when(request.getSession().getAttribute(SESSION_KEY)).thenReturn(null);

        final ModelAndView modelAndView = loginController.login(request, response);
        final View view = modelAndView.getView();

        modelAndView.renderView(request, response);

        assertThat(view).isInstanceOf(JspView.class);
        verify(response).sendRedirect("/index.jsp");
    }

    @DisplayName("잘못된 요청으로 로그인시 401.jsp 로 리다이렉트한다.")
    @Test
    void invalidLogin() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var session = mock(HttpSession.class);
        final LoginController loginController = new LoginController();

        when(request.getParameter("account")).thenReturn("notExistUser");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession()).thenReturn(session);
        when(request.getSession().getAttribute(SESSION_KEY)).thenReturn(null);

        final ModelAndView modelAndView = loginController.login(request, response);
        final View view = modelAndView.getView();

        modelAndView.renderView(request, response);

        assertThat(view).isInstanceOf(JspView.class);
        verify(response).sendRedirect("/401.jsp");
    }

    @DisplayName("로그인 상태이면 index.jsp 로 리다이렉트한다.")
    @Test
    void loggedIn() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var session = mock(HttpSession.class);
        final User user = new User(1L, "gugu", "password", "email");
        final LoginController loginController = new LoginController();

        when(request.getParameter("account")).thenReturn("gugu");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getRequestURI()).thenReturn("/login/view");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(request.getSession().getAttribute(SESSION_KEY)).thenReturn(user);

        final ModelAndView modelAndView = loginController.view(request, response);
        final View view = modelAndView.getView();

        modelAndView.renderView(request, response);

        assertThat(view).isInstanceOf(JspView.class);
        verify(response).sendRedirect("/index.jsp");
    }

    @DisplayName("로그인 상태가 아니면 로그인 페이지로 forward 한다.")
    @Test
    void notLoggedIn() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var session = mock(HttpSession.class);
        final RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        final LoginController loginController = new LoginController();

        when(request.getParameter("account")).thenReturn("gugu");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getRequestURI()).thenReturn("/login/view");
        when(request.getMethod()).thenReturn("GET");
        when(request.getSession()).thenReturn(session);
        when(request.getSession().getAttribute(SESSION_KEY)).thenReturn(null);
        when(request.getRequestDispatcher("/login.jsp")).thenReturn(requestDispatcher);

        final ModelAndView modelAndView = loginController.view(request, response);
        final View view = modelAndView.getView();

        modelAndView.renderView(request, response);

        assertThat(view).isInstanceOf(JspView.class);
        verify(requestDispatcher).forward(request, response);
    }
}
