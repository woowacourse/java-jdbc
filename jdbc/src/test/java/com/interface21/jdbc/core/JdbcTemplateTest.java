package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void updateFailureKeepsBoundConnectionOpen() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void updateClosesOwnedConnection() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update("update users set password = ?", "newPassword")).isEqualTo(1);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateFailureClosesOwnedConnection() throws SQLException {
        final var failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectClosesOwnedConnection() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("password")).thenReturn("password");

        final var actual = jdbcTemplate.queryForObject("select password from users", rs -> rs.getString("password"));

        assertThat(actual).isEqualTo("password");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectRejectsZeroRows() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select password from users", rs -> "password"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select password from users", rs -> "password"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void updateUsesBoundConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update("update users set password = ?", "newPassword")).isEqualTo(1);

        verify(statement).setObject(1, "newPassword");
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void queryForObjectUsesBoundConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("password")).thenReturn("password");

        final var actual = jdbcTemplate.queryForObject("select password from users", rs -> rs.getString("password"));

        assertThat(actual).isEqualTo("password");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void mappingFailureKeepsBoundConnectionOpen() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var failure = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select password from users", rs -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void connectionCloseFailurePreservesExecutionFailure() throws SQLException {
        final var failure = new SQLException("update failed");
        final var closeFailure = new SQLException("close failed");
        when(statement.executeUpdate()).thenThrow(failure);
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql"))
                .isInstanceOf(DataAccessException.class).hasCause(failure)
                .satisfies(exception -> {
                    assertThat(exception.getSuppressed()).hasSize(1);
                    assertThat(exception.getSuppressed()[0]).hasCause(closeFailure);
                });
    }

    @Test
    void connectionCloseFailureAfterSuccessIsReported() throws SQLException {
        final var failure = new SQLException("close failed");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("update users set password = ?", "newPassword"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
    }
}
