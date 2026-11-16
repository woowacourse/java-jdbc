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
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
        final var account = jdbcTemplate.queryForObject(SQL, row -> row.getString("account"),
                preparedStatement -> {});

        assertThat(account).isNull();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, row -> row.getString("account"),
                preparedStatement -> {}))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("found 2")
                .hasMessageContaining(SQL);

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

    @Test
    void updateBindsArgumentsInOrder() throws SQLException {
        final var sql = "update users set account = ?, email = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(sql, "gugu", null, 1L);

        assertThat(affectedRows).isEqualTo(1);
        final var order = inOrder(statement, connection);
        order.verify(statement).setObject(1, "gugu");
        order.verify(statement).setObject(2, null);
        order.verify(statement).setObject(3, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void updateWorksWithoutArguments() throws SQLException {
        final var sql = "delete from users";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(2);

        assertThat(jdbcTemplate.update(sql)).isEqualTo(2);

        verify(statement).executeUpdate();
        verify(statement).close();
        verifyNoMoreInteractions(statement);
    }

    @Test
    void updateTreatsNullArgumentArrayAsEmpty() throws SQLException {
        final var sql = "delete from users";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        jdbcTemplate.update(sql, (Object[]) null);

        verify(statement).executeUpdate();
        verify(statement).close();
        verifyNoMoreInteractions(statement);
    }

    @Test
    void queryBindsArgumentsBeforeReadingRows() throws SQLException {
        final var sql = "select account from users where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var accounts = jdbcTemplate.query(sql, row -> row.getString("account"), 1L);

        assertThat(accounts).containsExactly("gugu");
        final var order = inOrder(statement);
        order.verify(statement).setObject(1, 1L);
        order.verify(statement).executeQuery();
    }

    @Test
    void queryForObjectAcceptsArguments() throws SQLException {
        final var sql = "select account from users where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var account = jdbcTemplate.queryForObject(sql, row -> row.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
        verify(statement).setObject(1, 1L);
    }

    @Test
    void updateRunsSetterAndReturnsAffectedRowCount() throws SQLException {
        final var sql = "update users set account = ? where id > ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(3);

        final var affectedRows = jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, "gugu");
            preparedStatement.setLong(2, 1L);
        });

        assertThat(affectedRows).isEqualTo(3);
        final var order = inOrder(statement, connection);
        order.verify(statement).setString(1, "gugu");
        order.verify(statement).setLong(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void queryRunsSetterAndMapsAllRowsInOrder() throws SQLException {
        final var sql = "select account from users where account like ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "gugu2");

        final var accounts = jdbcTemplate.query(sql, row -> row.getString("account"),
                preparedStatement -> preparedStatement.setString(1, "gugu%"));

        assertThat(accounts).containsExactly("gugu", "gugu2");
        final var order = inOrder(statement, resultSet, connection);
        order.verify(statement).setString(1, "gugu%");
        order.verify(statement).executeQuery();
        order.verify(resultSet).next();
        order.verify(resultSet).getString("account");
        order.verify(resultSet).next();
        order.verify(resultSet).getString("account");
        order.verify(resultSet).next();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void queryForObjectAcceptsRowMapperBeforeSetter() throws SQLException {
        final var sql = "select account from users where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var account = jdbcTemplate.queryForObject(sql, row -> row.getString("account"),
                preparedStatement -> preparedStatement.setLong(1, 1L));

        assertThat(account).isEqualTo("gugu");
        verify(statement).setLong(1, 1L);
    }
}
