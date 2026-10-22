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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void updateBindsParametersAndReturnsAffectedRowCount() throws SQLException {
        final var sql = "update users set account = ?, email = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(2);

        final var count = jdbcTemplate.update(sql, "gugu", null, 1L);

        assertThat(count).isEqualTo(2);
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, null);
        verify(statement).setObject(3, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMapsEveryRowAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "brown");

        final var accounts = jdbcTemplate.query("select account from users where id > ?",
                row -> row.getString("account"), 0L);

        assertThat(accounts).containsExactly("gugu", "brown");
        verify(statement).setObject(1, 0L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryReturnsEmptyListWhenThereAreNoRows() {
        final var accounts = jdbcTemplate.query("select account from users", row -> row.getString("account"));

        assertThat(accounts).isEmpty();
    }

    @Test
    void queryForObjectReturnsTheMappedRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);

        final var id = jdbcTemplate.queryForObject("select id from users where account = ?",
                row -> row.getLong("id"), "gugu");

        assertThat(id).isEqualTo(1L);
        verify(statement).setObject(1, "gugu");
    }

    @Test
    void queryForObjectReturnsNullWhenThereAreNoRows() {
        final var id = jdbcTemplate.queryForObject("select id from users", row -> row.getLong("id"));

        assertThat(id).isNull();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        final var sql = "select id from users";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, row -> row.getLong("id")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("2")
                .hasMessageContaining(sql);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void connectionFailurePreservesSqlAndCause() throws SQLException {
        final var failure = new SQLException("Connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);
        final var sql = "delete from users";

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
    }

    @Test
    void prepareFailureClosesConnection() throws SQLException {
        final var sql = "delete from users";
        final var failure = new SQLException("Invalid SQL");
        when(connection.prepareStatement(sql)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(connection).close();
    }

    @Test
    void bindingFailureClosesStatementAndConnection() throws SQLException {
        final var failure = new SQLException("Unsupported parameter");
        doThrow(failure).when(statement).setObject(1, "gugu");

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users where account = ?", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateFailureClosesStatementAndConnection() throws SQLException {
        final var failure = new SQLException("Constraint violation");
        when(statement.executeUpdate()).thenThrow(failure);
        final var sql = "insert into users (account) values (?)";

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryFailureClosesStatementAndConnection() throws SQLException {
        final var failure = new SQLException("Invalid query");
        when(statement.executeQuery()).thenThrow(failure);
        final var sql = "select id from missing_table";

        assertThatThrownBy(() -> jdbcTemplate.query(sql, row -> row.getLong("id")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void sqlMappingFailureClosesAllResourcesAndPreservesCause() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        final var failure = new SQLException("Column unavailable");
        when(resultSet.getString("account")).thenThrow(failure);
        final var sql = "select account from users";

        assertThatThrownBy(() -> jdbcTemplate.query(sql, row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void runtimeMappingFailureAlsoClosesAllResources() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        final var failure = new IllegalArgumentException("Cannot map row");

        assertThatThrownBy(() -> jdbcTemplate.query("select id from users", row -> { throw failure; }))
                .isSameAs(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void cleanupFailureIsSuppressedWithoutReplacingTheSqlFailure() throws SQLException {
        final var failure = new SQLException("Execution failed");
        final var closeFailure = new SQLException("Close failed");
        when(statement.executeUpdate()).thenThrow(failure);
        doThrow(closeFailure).when(statement).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);
        assertThat(failure.getSuppressed()).containsExactly(closeFailure);
        verify(connection).close();
    }
}
