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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void updateBindsParametersAndReturnsAffectedRows() throws SQLException {
        when(statement.executeUpdate()).thenReturn(2);

        final var affectedRows = jdbcTemplate.update(
                "update users set password = ?, email = ? where id = ?", "newPassword", null, 1L);

        assertThat(affectedRows).isEqualTo(2);
        verify(statement).setObject(1, "newPassword");
        verify(statement).setObject(2, null);
        verify(statement).setObject(3, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMapsEveryRowAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        final var accounts = jdbcTemplate.query(
                "select account from users where id > ?", rs -> rs.getString("account"), 0L);

        assertThat(accounts).containsExactly("gugu", "other");
        verify(statement).setObject(1, 0L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectReturnsSingleRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", rs -> rs.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectReturnsNullWhenNoRowsMatch() throws SQLException {
        final var account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", rs -> rs.getString("account"), 99L);

        assertThat(account).isNull();
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectRejectsMultipleRowsAsSoonAsSecondRowIsFound() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users", rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("found at least 2");
        verify(resultSet, times(2)).next();
        verify(resultSet).getString("account");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectMappingSqlExceptionIsTranslatedAndClosesResources() throws SQLException {
        final var cause = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users", rs -> {
            throw cause;
        })).isInstanceOf(DataAccessException.class).hasCause(cause);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateTranslatesSqlExceptionAndClosesResources() throws SQLException {
        final var cause = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("update users set password = ?", "newPassword"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void connectionFailureIsTranslated() throws SQLException {
        final var cause = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    void statementCreationFailureClosesConnection() throws SQLException {
        final var cause = new SQLException("prepare failed");
        when(connection.prepareStatement(anyString())).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(connection).close();
    }

    @Test
    void mappingSqlExceptionIsTranslatedAndClosesResources() throws SQLException {
        final var cause = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rs -> {
            throw cause;
        })).isInstanceOf(DataAccessException.class).hasCause(cause);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void mappingRuntimeExceptionIsPreservedAndClosesResources() throws SQLException {
        final var cause = new IllegalStateException("mapping failed");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rs -> {
            throw cause;
        })).isSameAs(cause);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
