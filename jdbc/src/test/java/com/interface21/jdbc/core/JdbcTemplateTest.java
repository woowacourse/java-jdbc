package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    void updateUsesCustomSetterForTypedParameters() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update("update users set email = ? where id = ?", ps -> {
            ps.setNull(1, Types.VARCHAR);
            ps.setLong(2, 1L);
        });

        assertThat(affectedRows).isEqualTo(1);
        verify(statement).setNull(1, Types.VARCHAR);
        verify(statement).setLong(2, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryUsesCustomSetterAndMapsEveryRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        final var accounts = jdbcTemplate.query("select account from users where id > ?",
                ps -> ps.setLong(1, 0L), rs -> rs.getString("account"));

        assertThat(accounts).containsExactly("gugu", "other");
        verify(statement).setLong(1, 0L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectUsesCustomSetter() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var account = jdbcTemplate.queryForObject("select account from users where id = ?",
                ps -> ps.setLong(1, 1L), rs -> rs.getString("account"));

        assertThat(account).isEqualTo("gugu");
        verify(statement).setLong(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateDoesNotExecuteWhenCustomSetterThrowsSqlException() throws SQLException {
        final var cause = new SQLException("binding failed");

        assertThatThrownBy(() -> jdbcTemplate.update("update users set email = ?", ps -> {
            throw cause;
        })).isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryDoesNotExecuteWhenCustomSetterThrowsSqlException() throws SQLException {
        final var cause = new SQLException("binding failed");

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users where id = ?", ps -> {
            throw cause;
        }, rs -> rs.getString("account"))).isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement, never()).executeQuery();
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
    void varargsBindingFailureDoesNotExecuteAndClosesResources() throws SQLException {
        final var cause = new SQLException("binding failed");
        doThrow(cause).when(statement).setObject(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users where id = ?",
                rs -> rs.getString("account"), 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeQuery();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryExecutionFailureIsTranslatedAndClosesResources() throws SQLException {
        final var cause = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("select account from users")
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void customSetterRuntimeExceptionIsPreservedAndClosesResources() throws SQLException {
        final var cause = new IllegalArgumentException("invalid parameter");

        assertThatThrownBy(() -> jdbcTemplate.update("update users set email = ?", ps -> {
            throw cause;
        })).isSameAs(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void executionFailureIsPreservedWhenClosingResourcesAlsoFails() throws SQLException {
        final var cause = new SQLException("update failed");
        final var statementCloseFailure = new SQLException("statement close failed");
        final var connectionCloseFailure = new SQLException("connection close failed");
        when(statement.executeUpdate()).thenThrow(cause);
        doThrow(statementCloseFailure).when(statement).close();
        doThrow(connectionCloseFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        assertThat(cause.getSuppressed()).containsExactly(statementCloseFailure, connectionCloseFailure);
        verify(statement).close();
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
