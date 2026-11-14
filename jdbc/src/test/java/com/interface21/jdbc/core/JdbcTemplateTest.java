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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "select account from users";

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void updateClosesResourcesWhenSettingValuesFails() throws SQLException {
        final var cause = new SQLException("Failed to set a value");

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, preparedStatement -> {
            throw cause;
        })).isInstanceOf(DataAccessException.class).hasCause(cause);

        final var order = inOrder(statement, connection);
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void queryClosesResourcesWhenMappingFails() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        final var cause = new SQLException("Failed to read a column");

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, row -> {
            throw cause;
        })).isInstanceOf(DataAccessException.class).hasCause(cause);

        final var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void queryReturnsEmptyListWhenNoRowsExist() {
        assertThat(jdbcTemplate.query(SQL, row -> row.getString("account"))).isEmpty();
    }

    @Test
    void queryForObjectReturnsNullWhenNoRowsExist() {
        final var account = jdbcTemplate.queryForObject(SQL, preparedStatement -> {},
                row -> row.getString("account"));

        assertThat(account).isNull();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, preparedStatement -> {},
                row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("found 2");

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void closesConnectionWhenPreparingStatementFails() throws SQLException {
        final var cause = new SQLException("Invalid SQL");
        when(connection.prepareStatement(SQL)).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, preparedStatement -> {}))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
    }
}
