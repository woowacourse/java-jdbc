package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @AfterEach
    void unbindConnection() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void updateBindsArgumentsAndReturnsAffectedRows() throws SQLException {
        final var sql = "update users set account = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update(sql, "gugu", 7L)).isEqualTo(1);

        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, 7L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateAcceptsCustomSetter() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update("update users set account = ? where id = ?", stmt -> {
            stmt.setNull(1, Types.VARCHAR);
            stmt.setLong(2, 7L);
        })).isEqualTo(1);

        verify(statement).setNull(1, Types.VARCHAR);
        verify(statement).setLong(2, 7L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void setterFailurePreservesCauseAndClosesResources() throws SQLException {
        final var cause = new SQLException("binding failed");

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users where id = ?", stmt -> {
            throw cause;
        })).isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMapsEveryRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        assertThat(jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L)).containsExactly("gugu", "other");

        verify(connection).prepareStatement(SQL);
        verify(statement).setObject(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void emptyQueryReturnsEmptyList() throws SQLException {
        assertThat(jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L)).isEmpty();

        verify(statement).setObject(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void emptyQueryWithCustomSetterReturnsEmptyList() throws SQLException {
        assertThat(jdbcTemplate.query(SQL, stmt -> stmt.setLong(1, 7L), ACCOUNT_MAPPER)).isEmpty();

        verify(statement).setLong(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsSingleRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.queryForObject(SQL, ACCOUNT_MAPPER, 7L)).isEqualTo("gugu");

        verify(statement).setObject(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectWithCustomSetterReturnsSingleRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.queryForObject(SQL, stmt -> stmt.setLong(1, 7L), ACCOUNT_MAPPER))
                .isEqualTo("gugu");
        verify(statement).setLong(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsNullWhenEmpty() throws SQLException {
        assertThat(jdbcTemplate.queryForObject(SQL, ACCOUNT_MAPPER, 7L)).isNull();

        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, ACCOUNT_MAPPER, 7L))
                .isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessageContaining(SQL);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryFailurePreservesCauseAndClosesResources() throws SQLException {
        final var cause = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L))
                .isExactlyInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void mappingFailurePreservesCauseAndClosesResources() throws SQLException {
        final var cause = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void updateFailurePreservesCauseAndClosesResources() throws SQLException {
        final var cause = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void preparationFailureClosesAcquiredConnection() throws SQLException {
        final var cause = new SQLException("prepare failed");
        when(connection.prepareStatement(SQL)).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ACCOUNT_MAPPER, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(connection).close();
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateReusesBoundConnectionAndKeepsItOpen() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update("update users set account = ?", "gugu")).isEqualTo(1);

        verify(statement).setObject(1, "gugu");
        verify(statement).close();
        verifyCallerOwnsConnection();
    }

    @Test
    void updateFailureKeepsBoundConnectionOpenForRollback() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var cause = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement).close();
        verifyCallerOwnsConnection();
    }

    @Test
    void queryForObjectClosesResultsAndKeepsBoundConnectionOpen() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.queryForObject(SQL, ACCOUNT_MAPPER, 7L)).isEqualTo("gugu");

        verify(statement).setObject(1, 7L);
        verify(resultSet).close();
        verify(statement).close();
        verifyCallerOwnsConnection();
    }

    private void verifyCallerOwnsConnection() throws SQLException {
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection, never()).setAutoCommit(anyBoolean());
    }
}
