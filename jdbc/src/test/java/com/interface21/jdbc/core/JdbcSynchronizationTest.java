package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcSynchronizationTest {

    private JdbcDataSource database;
    private DataSource dataSource;
    private Connection connection;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        database = new JdbcDataSource();
        database.setUrl("jdbc:h2:mem:jdbc_synchronization_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        try (var setupConnection = database.getConnection();
             var statement = setupConnection.createStatement()) {
            statement.execute("create table users (id bigint primary key, account varchar(100))");
            statement.executeUpdate("insert into users (id, account) values (1, 'gugu')");
        }
        connection = spy(database.getConnection());
        dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
        if (!connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void standaloneOperationsReleaseTheirUnboundConnection() throws SQLException {
        jdbcTemplate.update("update users set account = ? where id = ?", "changed", 1L);

        assertThat(connection.isClosed()).isTrue();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
        final var account = new JdbcTemplate(database).queryForObject(
                "select account from users where id = ?", rs -> rs.getString("account"), 1L);
        assertThat(account).isEqualTo("changed");
    }

    @Test
    void standaloneSqlFailureStillReleasesTheConnection() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.update("update missing_table set account = ?", "changed"))
                .isInstanceOf(DataAccessException.class);

        assertThat(connection.isClosed()).isTrue();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        verify(connection).close();
    }

    @Test
    void ordinaryTemplateMethodsShareTheBoundConnectionWithoutClosingIt() throws SQLException {
        connection.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        jdbcTemplate.update("update users set account = ? where id = ?", "changed", 1L);
        final var account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", rs -> rs.getString("account"), 1L);

        assertThat(account).isEqualTo("changed");
        final var committedAccount = new JdbcTemplate(database).queryForObject(
                "select account from users where id = ?", rs -> rs.getString("account"), 1L);
        assertThat(committedAccount).isEqualTo("gugu");
        assertThat(connection.isClosed()).isFalse();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
        connection.rollback();
    }

    @Test
    void mapperFailureLeavesTheBoundConnectionForItsOwnerToRollBack() throws SQLException {
        connection.setAutoCommit(false);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var failure = new IllegalStateException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rs -> { throw failure; }))
                .isSameAs(failure);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        assertThat(connection.isClosed()).isFalse();
        verify(connection, never()).close();
        connection.rollback();
    }
}
