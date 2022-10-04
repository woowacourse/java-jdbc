package com.techcourse.controller;

import static com.techcourse.controller.UserSession.SESSION_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techcourse.domain.User;
import com.techcourse.repository.InMemoryUserRepository;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nextstep.mvc.view.JspView;
import nextstep.mvc.view.ModelAndView;
import nextstep.mvc.view.View;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegisterControllerTest {

    @DisplayName("RegisterController 를 통해서 회원가입을 할 수 있다.")
    @Test
    void register() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var session = mock(HttpSession.class);
        final RegisterController registerController = new RegisterController();

        when(request.getParameter("account")).thenReturn("dwoo");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("email")).thenReturn("email@email.com");
        when(request.getRequestURI()).thenReturn("/register");
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession()).thenReturn(session);
        when(request.getSession().getAttribute(SESSION_KEY)).thenReturn(null);

        final ModelAndView modelAndView = registerController.register(request, response);
        final View view = modelAndView.getView();

        modelAndView.renderView(request, response);

        final User user = InMemoryUserRepository.findByAccount("dwoo").orElseThrow();
        assertThat(user).extracting("account", "password", "email")
                .contains("dwoo", "password", "email@email.com");
        assertThat(view).isInstanceOf(JspView.class);
        verify(response).sendRedirect("/index.jsp");
    }

    @DisplayName("회원가입 뷰를 보여줄 수 있다.")
    @Test
    void renderRegisterView() throws Exception {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final RequestDispatcher requestDispatcher = mock(RequestDispatcher.class);
        final RegisterController registerController = new RegisterController();

        when(request.getRequestDispatcher("/register.jsp")).thenReturn(requestDispatcher);
        when(request.getRequestURI()).thenReturn("/register/view");
        when(request.getMethod()).thenReturn("GET");

        final ModelAndView modelAndView = registerController.view(request, response);
        final View view = modelAndView.getView();

        modelAndView.renderView(request, response);

        assertThat(view).isInstanceOf(JspView.class);
        verify(requestDispatcher).forward(request, response);
    }
}
