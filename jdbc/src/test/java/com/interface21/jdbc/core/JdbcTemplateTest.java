package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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

        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsSingleRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.queryForObject(SQL, ACCOUNT_MAPPER, 7L)).isEqualTo("gugu");
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
}
