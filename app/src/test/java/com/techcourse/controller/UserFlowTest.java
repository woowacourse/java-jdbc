package com.techcourse.controller;

import com.interface21.dao.DuplicateKeyException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserFlowTest {

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final HttpSession session = mock(HttpSession.class);

    @BeforeEach
    void setup() throws SQLException {
        assertThat(((org.h2.jdbcx.JdbcDataSource) DataSourceConfig.getInstance()).getURL())
                .as("초기화는 테스트 메모리 DB에서만 허용합니다. jdbc.url 설정을 확인하세요.")
                .startsWith("jdbc:h2:mem:");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        try (var connection = DataSourceConfig.getInstance().getConnection();
             var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        }
        when(request.getSession()).thenReturn(session);
    }

    @Test
    void registersQueriesAndLogsInUsingDatabase() throws Exception {
        register("first", "secret", "first@example.com");
        register("second", "other", "second@example.com");

        final var dao = new UserDao(DataSourceConfig.getInstance());
        assertThat(dao.findAll()).hasSize(2);
        assertThat(dao.findByAccount("first").getId())
                .isNotEqualTo(dao.findByAccount("second").getId());

        when(request.getParameter("account")).thenReturn("first");
        final var model = new UserController().show(request, response);
        final var user = (User) model.getObject("user");
        assertThat(user.getAccount()).isEqualTo("first");
        assertThat(user.getEmail()).isEqualTo("first@example.com");

        when(request.getParameter("password")).thenReturn("secret");
        final var login = new LoginController().login(request, response);
        login.getView().render(login.getModel(), request, response);
        verify(session).setAttribute(eq(UserSession.SESSION_KEY), argThat(value ->
                value instanceof User loggedIn && loggedIn.getId() == user.getId()));
        verify(response).sendRedirect("/index.jsp");
    }

    @Test
    void rejectsUnknownAccount() throws Exception {
        when(request.getParameter("account")).thenReturn("missing");
        assertLoginRejected();
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        register("first", "secret", "first@example.com");
        when(request.getParameter("password")).thenReturn("wrong");
        assertLoginRejected();
    }

    @Test
    void duplicateRegistrationKeepsExistingAccount() throws Exception {
        register("first", "secret", "first@example.com");
        when(request.getParameter("password")).thenReturn("replacement");
        when(request.getParameter("email")).thenReturn("replacement@example.com");
        final var registration = new RegisterController().register(request, response);
        registration.getView().render(registration.getModel(), request, response);
        verify(response).sendRedirect("/register");

        final var dao = new UserDao(DataSourceConfig.getInstance());
        assertThat(dao.findAll()).hasSize(1);
        assertThat(dao.findByAccount("first").getEmail()).isEqualTo("first@example.com");
        when(request.getParameter("password")).thenReturn("secret");
        final var login = new LoginController().login(request, response);
        login.getView().render(login.getModel(), request, response);
        verify(response).sendRedirect("/index.jsp");
    }

    @Test
    void databaseAlsoRejectsDuplicateAccounts() {
        register("first", "secret", "first@example.com");
        final var dao = new UserDao(DataSourceConfig.getInstance());
        assertThatThrownBy(() -> dao.insert(new User("first", "other", "other@example.com")))
                .isInstanceOf(DuplicateKeyException.class).hasCauseInstanceOf(SQLException.class);
        assertThat(dao.findAll()).hasSize(1);
    }

    @Test
    void duplicateInsertAfterPassingPreCheckRedirectsToRegister() throws Exception {
        final var dao = mock(UserDao.class);
        when(dao.findByAccount("first")).thenReturn(null);
        doThrow(new DuplicateKeyException("duplicate", new SQLException("unique index violated", "23505")))
                .when(dao).insert(any(User.class));
        when(request.getParameter("account")).thenReturn("first");
        when(request.getParameter("password")).thenReturn("secret");
        when(request.getParameter("email")).thenReturn("first@example.com");

        final var registration = new RegisterController(dao).register(request, response);
        registration.getView().render(registration.getModel(), request, response);

        verify(response).sendRedirect("/register");
    }

    @Test
    void missingPasswordRedirectsToRegisterWithoutSaving() throws Exception {
        when(request.getParameter("account")).thenReturn("first");
        when(request.getParameter("password")).thenReturn(null);
        when(request.getParameter("email")).thenReturn("first@example.com");

        final var registration = new RegisterController().register(request, response);
        registration.getView().render(registration.getModel(), request, response);

        verify(response).sendRedirect("/register");
        assertThat(new UserDao(DataSourceConfig.getInstance()).findAll()).isEmpty();
    }

    @Test
    void missingUserKeepsExistingFailureBehavior() {
        when(request.getParameter("account")).thenReturn("missing");
        assertThatThrownBy(() -> new UserController().show(request, response))
                .isInstanceOf(NoSuchElementException.class);
    }

    private void register(final String account, final String password, final String email) {
        when(request.getParameter("account")).thenReturn(account);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("email")).thenReturn(email);
        new RegisterController().register(request, response);
    }

    private void assertLoginRejected() throws Exception {
        final var login = new LoginController().login(request, response);
        login.getView().render(login.getModel(), request, response);
        verify(response).sendRedirect("/401.jsp");
        verify(session, never()).setAttribute(anyString(), any());
    }
}
