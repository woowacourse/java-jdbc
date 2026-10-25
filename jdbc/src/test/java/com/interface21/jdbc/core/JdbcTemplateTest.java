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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "select account from users where id = ?";
    private static final RowMapper<String> ACCOUNT_MAPPER = rs -> rs.getString("account");

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void updateBindsArgumentsInOrderAndReturnsAffectedRows() throws SQLException {
        final var sql = "update users set account = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(2);

        assertThat(jdbcTemplate.update(sql, "gugu", 7L)).isEqualTo(2);

        final var order = inOrder(connection, statement);
        order.verify(connection).prepareStatement(sql);
        order.verify(statement).setObject(1, "gugu");
        order.verify(statement).setObject(2, 7L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void updateWithoutArgumentsCanReturnZero() throws SQLException {
        assertThat(jdbcTemplate.update("delete from users")).isZero();

        verify(statement).executeUpdate();
        verifyStatementResourcesClosed();
    }

    @Test
    void nullParameterIsBoundAsSqlNull() throws SQLException {
        jdbcTemplate.update("update users set account = ?", (Object) null);

        verify(statement).setObject(1, null);
    }

    @Test
    void queryBindsArgumentsAndMapsEveryRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        assertThat(jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L)).containsExactly("gugu", "other");

        final var order = inOrder(connection, statement, resultSet);
        order.verify(connection).prepareStatement(SQL);
        order.verify(statement).setObject(1, 7L);
        order.verify(statement).executeQuery();
        verifyQueryResourcesClosed();
    }

    @Test
    void emptyQueryReturnsEmptyListWithoutMapping() throws SQLException {
        assertThat(jdbcTemplate.query("select account from users", ACCOUNT_MAPPER)).isEmpty();

        verify(resultSet, never()).getString("account");
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsSingleMappedRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.queryForObject(SQL, ACCOUNT_MAPPER, 7L)).isEqualTo("gugu");

        verify(statement).setObject(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsNullWhenNoRowsExist() throws SQLException {
        assertThat(jdbcTemplate.queryForObject(SQL, ACCOUNT_MAPPER, 7L)).isNull();

        verify(resultSet, never()).getString("account");
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectStopsAtSecondRowWithoutMappingIt() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, ACCOUNT_MAPPER, 7L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("more than one row");

        verify(resultSet, times(2)).next();
        verify(resultSet, times(1)).getString("account");
        verifyQueryResourcesClosed();
    }

    @Test
    void connectionFailurePreservesSqlAndCause() throws SQLException {
        final var cause = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, 7L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(SQL)
                .hasCause(cause);

        verify(connection, never()).close();
    }

    @Test
    void preparationFailureClosesAcquiredConnection() throws SQLException {
        final var cause = new SQLException("prepare failed");
        when(connection.prepareStatement(SQL)).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(connection).close();
        verify(statement, never()).close();
    }

    @Test
    void bindingFailureClosesStatementAndConnection() throws SQLException {
        final var cause = new SQLException("binding failed");
        doThrow(cause).when(statement).setObject(1, 7L);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement, never()).executeUpdate();
        verifyStatementResourcesClosed();
    }

    @Test
    void updateFailurePreservesCauseWithoutIncludingArgumentsInMessage() throws SQLException {
        final var sql = "update users set password = ?";
        final var cause = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "secret-password"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasMessageNotContaining("secret-password")
                .hasCause(cause);

        verifyStatementResourcesClosed();
    }

    @Test
    void queryExecutionFailureClosesStatementAndConnection() throws SQLException {
        final var cause = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyStatementResourcesClosed();
    }

    @Test
    void cursorFailureClosesAllQueryResources() throws SQLException {
        final var cause = new SQLException("cursor failed");
        when(resultSet.next()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void sqlMappingFailureClosesAllQueryResources() throws SQLException {
        final var cause = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void runtimeMappingFailurePropagatesAndClosesAllQueryResources() throws SQLException {
        final var cause = new IllegalArgumentException("invalid account");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, ACCOUNT_MAPPER, 7L))
                .isSameAs(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void closeFailureStillClosesRemainingResources() throws SQLException {
        final var cause = new SQLException("result set close failed");
        doThrow(cause).when(resultSet).close();

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void executionFailureRemainsCauseWhenClosingAlsoFails() throws SQLException {
        final var cause = new SQLException("update failed");
        final var closeFailure = new SQLException("statement close failed");
        when(statement.executeUpdate()).thenThrow(cause);
        doThrow(closeFailure).when(statement).close();

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);
        assertThat(cause.getSuppressed()).containsExactly(closeFailure);

        verifyStatementResourcesClosed();
    }

    private void verifyStatementResourcesClosed() throws SQLException {
        verify(statement).close();
        verify(connection).close();
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        final var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }
}
