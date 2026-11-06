package com.techcourse.support.context;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.config.UserServiceConfig;
import com.techcourse.domain.User;
import com.techcourse.service.UserService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextLoaderListenerTest {

    @AfterEach
    void tearDown() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.executeUpdate("truncate table user_history restart identity");
            statement.executeUpdate("truncate table users restart identity");
        }
    }

    @Test
    void registersServiceThatCommitsPasswordAndHistoryTogether() throws SQLException {
        final var servletContext = mock(ServletContext.class);
        new ContextLoaderListener().contextInitialized(new ServletContextEvent(servletContext));
        final var captor = ArgumentCaptor.forClass(UserService.class);
        verify(servletContext).setAttribute(eq(UserService.class.getName()), captor.capture());
        final var service = captor.getValue();
        assertThat(service).isSameAs(UserServiceConfig.getInstance());
        service.save(new User("context-user", "password", "context@example.com"));
        final long userId;
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.prepareStatement("select id from users where account = ?")) {
            statement.setString(1, "context-user");
            try (final var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                userId = resultSet.getLong("id");
            }
        }

        service.changePassword(userId, "newPassword", "context-user");

        assertThat(service.findById(userId).getPassword()).isEqualTo("newPassword");
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.prepareStatement("select password, created_by from user_history where user_id = ?")) {
            statement.setLong(1, userId);
            try (final var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("password")).isEqualTo("newPassword");
                assertThat(resultSet.getString("created_by")).isEqualTo("context-user");
                assertThat(resultSet.next()).isFalse();
            }
        }
    }
}
