package com.techcourse.controller;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final HttpSession session = mock(HttpSession.class);

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        }
        when(request.getSession()).thenReturn(session);
    }

    @Test
    void registerAndShowUseDatabase() {
        registerUser();

        final var saved = new UserDao(DataSourceConfig.getInstance()).findByAccount("db-user");
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(1L);

        final var modelAndView = new UserController().show(request, response);
        final var actual = (User) modelAndView.getObject("user");

        assertThat(actual.getId()).isEqualTo(saved.getId());
        assertThat(actual.getAccount()).isEqualTo("db-user");
        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(actual.getEmail()).isEqualTo("db@example.com");
    }

    @Test
    void loginWithRegisteredUser() throws Exception {
        registerUser();

        final var modelAndView = new LoginController().login(request, response);
        modelAndView.getView().render(modelAndView.getModel(), request, response);

        final var user = ArgumentCaptor.forClass(User.class);
        verify(session).setAttribute(eq(UserSession.SESSION_KEY), user.capture());
        assertThat(user.getValue().getAccount()).isEqualTo("db-user");
        assertThat(user.getValue().getId()).isEqualTo(1L);
        verify(response).sendRedirect("/index.jsp");
    }

    @Test
    void loginRejectsWrongPasswordAndUnknownAccount() throws Exception {
        registerUser();
        final var controller = new LoginController();
        when(request.getParameter("password")).thenReturn("wrong-password");

        final var wrongPassword = controller.login(request, response);
        wrongPassword.getView().render(wrongPassword.getModel(), request, response);

        when(request.getParameter("account")).thenReturn("unknown");
        when(request.getParameter("password")).thenReturn("password");
        final var unknownAccount = controller.login(request, response);
        unknownAccount.getView().render(unknownAccount.getModel(), request, response);

        verify(response, times(2)).sendRedirect("/401.jsp");
        verify(session, never()).setAttribute(eq(UserSession.SESSION_KEY), any());
    }

    private void registerUser() {
        when(request.getParameter("account")).thenReturn("db-user");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("email")).thenReturn("db@example.com");
        new RegisterController().register(request, response);
    }
}
