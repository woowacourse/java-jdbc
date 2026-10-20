package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void updateWithSetterSupportsExplicitSqlTypes() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int updatedRows = jdbcTemplate.update("update users set email = ? where id = ?", preparedStatement -> {
            preparedStatement.setNull(1, Types.VARCHAR);
            preparedStatement.setLong(2, 42L);
        });

        assertThat(updatedRows).isEqualTo(1);
        final InOrder executionOrder = inOrder(statement);
        executionOrder.verify(statement).setNull(1, Types.VARCHAR);
        executionOrder.verify(statement).setLong(2, 42L);
        executionOrder.verify(statement).executeUpdate();
    }

    @Test
    void updateWithVarargsBindsValuesInOrderIncludingNull() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int updatedRows = jdbcTemplate.update(
                "update users set account = ?, email = ? where id = ?", "gugu", null, 42L);

        assertThat(updatedRows).isEqualTo(1);
        final InOrder executionOrder = inOrder(statement);
        executionOrder.verify(statement).setObject(1, "gugu");
        executionOrder.verify(statement).setObject(2, null);
        executionOrder.verify(statement).setObject(3, 42L);
        executionOrder.verify(statement).executeUpdate();
    }

    @Test
    void queryWithSetterBindsValuesBeforeMappingRows() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "gugu2");

        final List<String> accounts = jdbcTemplate.query(
                "select account from users where account like ?",
                row -> row.getString("account"),
                preparedStatement -> preparedStatement.setString(1, "gugu%"));

        assertThat(accounts).containsExactly("gugu", "gugu2");
        final InOrder executionOrder = inOrder(statement);
        executionOrder.verify(statement).setString(1, "gugu%");
        executionOrder.verify(statement).executeQuery();
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectWithSetterBindsValuesAndReturnsObject() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String account = jdbcTemplate.queryForObject(
                "select account from users where id = ?",
                row -> row.getString("account"),
                preparedStatement -> preparedStatement.setLong(1, 42L));

        assertThat(account).isEqualTo("gugu");
        verify(statement).setLong(1, 42L);
    }

    @Test
    void closesResourcesWithoutExecutingSqlWhenSetterFails() throws SQLException {
        final SQLException failure = new SQLException("Parameter binding failed");
        final PreparedStatementSetter setter = preparedStatement -> {
            throw failure;
        };

        assertThatThrownBy(() -> jdbcTemplate.update("update users set email = ?", setter))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectReturnsNullWhenNoRowsAreReturned() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final String result = jdbcTemplate.queryForObject(
                "select account from users", row -> row.getString("account"));

        assertThat(result).isNull();
    }

    @Test
    void queryForObjectReturnsObjectWhenOneRowIsReturned() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String result = jdbcTemplate.queryForObject(
                "select account from users", row -> row.getString("account"));

        assertThat(result).isEqualTo("gugu");
    }

    @Test
    void queryForObjectThrowsWhenMultipleRowsAreReturned() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "another-user");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users", row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Expected at most 1 result, but found 2");

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void closesResourcesWhenUpdateFails() throws SQLException {
        final SQLException failure = new SQLException("Update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("insert into users (account) values (?)", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void closesResourcesWhenMappingFails() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        final SQLException failure = new SQLException("Mapping failed");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        final RowMapper<String> rowMapper = row -> {
            throw failure;
        };

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void closesConnectionWhenStatementCreationFails() throws SQLException {
        final SQLException failure = new SQLException("Statement creation failed");
        when(connection.prepareStatement(anyString())).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).close();
    }
}
