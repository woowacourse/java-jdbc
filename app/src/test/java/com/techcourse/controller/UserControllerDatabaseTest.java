package com.techcourse.controller;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerDatabaseTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        final var dataSource = DataSourceConfig.getInstance();
        final var jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("drop table if exists user_history");
        jdbcTemplate.update("drop table if exists users");
        DatabasePopulatorUtils.execute(dataSource);
        userDao = new UserDao(jdbcTemplate);
    }

    @Test
    @DisplayName("회원 가입하면 회원 정보를 DB에 저장한다")
    void registerSavesUserInDatabase() {
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        when(request.getParameter("account")).thenReturn("db-register-user");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("email")).thenReturn("db-user@example.com");

        new RegisterController().register(request, response);

        final var savedUser = userDao.findByAccount("db-register-user");
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).isEqualTo("password");
        assertThat(savedUser.getEmail()).isEqualTo("db-user@example.com");
    }

    @Test
    @DisplayName("DB에 저장된 회원은 로그인할 수 있다")
    void loginFindsUserInDatabase() {
        userDao.insert(new User("db-login-user", "password", "db-login-user@example.com"));
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        final var session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getParameter("account")).thenReturn("db-login-user");
        when(request.getParameter("password")).thenReturn("password");

        new LoginController().login(request, response);

        final var savedUser = ArgumentCaptor.forClass(Object.class);
        verify(session).setAttribute(eq(UserSession.SESSION_KEY), savedUser.capture());
        assertThat(savedUser.getValue()).isInstanceOf(User.class);
        assertThat(((User) savedUser.getValue()).getAccount()).isEqualTo("db-login-user");
    }

    @Test
    @DisplayName("회원 API는 DB에 저장된 회원을 돌려준다")
    void showFindsUserInDatabase() {
        userDao.insert(new User("db-api-user", "password", "db-api-user@example.com"));
        final var request = mock(HttpServletRequest.class);
        final var response = mock(HttpServletResponse.class);
        when(request.getParameter("account")).thenReturn("db-api-user");

        final var result = new UserController().show(request, response);

        final var user = (User) result.getObject("user");
        assertThat(user.getAccount()).isEqualTo("db-api-user");
        assertThat(user.getEmail()).isEqualTo("db-api-user@example.com");
    }
}
