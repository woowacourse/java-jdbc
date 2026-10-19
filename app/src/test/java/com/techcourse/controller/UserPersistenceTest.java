package com.techcourse.controller;

import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserPersistenceTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:users-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        DatabasePopulatorUtils.execute(dataSource);
        userDao = new UserDao(dataSource);
    }

    @Test
    void registerAndLoginWithDatabase() {
        final var registerRequest = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        when(registerRequest.getParameter("account")).thenReturn("moka");
        when(registerRequest.getParameter("password")).thenReturn("password");
        when(registerRequest.getParameter("email")).thenReturn("moka@example.com");

        new RegisterController(userDao).register(registerRequest, response);

        final var savedUser = userDao.findByAccount("moka");
        assertThat(savedUser).isNotNull();

        final var loginRequest = mock(HttpServletRequest.class);
        final var session = mock(HttpSession.class);

        when(loginRequest.getSession()).thenReturn(session);
        when(loginRequest.getParameter("account")).thenReturn("moka");
        when(loginRequest.getParameter("password")).thenReturn("password");

        new LoginController(userDao).login(loginRequest, response);

        final var captor = ArgumentCaptor.forClass(User.class);
        verify(session).setAttribute(eq(UserSession.SESSION_KEY), captor.capture());

        assertThat(captor.getValue().getAccount()).isEqualTo("moka");
    }

    @Test
    void findUserWithDatabase() {
        userDao.insert(new User("moka", "password", "moka@example.com"));

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        when(request.getParameter("account")).thenReturn("moka");

        final var modelAndView = new UserController(userDao).show(request, response);
        final var user = (Map<?, ?>) modelAndView.getObject("user");

        assertThat(user.get("account")).isEqualTo("moka");
        assertThat(user.get("email")).isEqualTo("moka@example.com");
        assertThat(user.containsKey("password")).isFalse();
    }

    @Test
    void duplicateAccountCannotBeRegistered() {
        userDao.insert(new User("moka", "oldPassword", "moka@example.com"));

        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);

        when(request.getParameter("account")).thenReturn("moka");
        when(request.getParameter("password")).thenReturn("newPassword");
        when(request.getParameter("email")).thenReturn("new@example.com");

        final var modelAndView = new RegisterController(userDao)
                .register(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);

        assertThat(modelAndView.getObject("errorMessage"))
                .isEqualTo("이미 사용 중인 아이디입니다.");

        assertThat(userDao.findAll()).hasSize(1);
        assertThat(userDao.findByAccount("moka").getPassword())
                .isEqualTo("oldPassword");
    }
}
