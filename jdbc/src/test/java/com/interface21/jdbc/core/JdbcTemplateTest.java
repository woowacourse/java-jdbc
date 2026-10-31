package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void updateBindsParametersAndReturnsAffectedRows() throws SQLException {
        final var sql = "update users set email = ?, password = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(sql, "gugu@example.com", null, 1L);

        assertThat(affectedRows).isEqualTo(1);
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, "gugu@example.com");
        verify(statement).setObject(2, null);
        verify(statement).setObject(3, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateClosesResourcesAndPreservesSqlException() throws SQLException {
        final var cause = new SQLException("execution failed");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryBindsParametersAndMapsEveryRow() throws SQLException {
        final var sql = "select account from users where id > ?";
        final var resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        final var accounts = jdbcTemplate.query(sql, row -> row.getString("account"), 0L);

        assertThat(accounts).containsExactly("gugu", "other");
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, 0L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryReturnsEmptyListWhenNoRowsExist() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);

        final var accounts = jdbcTemplate.query("select account from users", row -> row.getString("account"));

        assertThat(accounts).isEmpty();
        verify(resultSet, never()).getString(anyString());
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryClosesResourcesWhenMappingFails() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        final var cause = new SQLException("mapping failed");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", row -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryClosesResourcesWhenExecutionFails() throws SQLException {
        final var cause = new SQLException("execution failed");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }
}
