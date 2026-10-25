package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doThrow;
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
    void setup() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void constructorRejectsNullDataSource() {
        assertThatThrownBy(() -> new JdbcTemplate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("dataSource must not be null");
    }

    @Test
    void updateRejectsNullSetterBeforeOpeningConnection() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "update users set email = ?", (PreparedStatementSetter) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("setter must not be null");

        verify(dataSource, never()).getConnection();
    }

    @Test
    void queryRejectsNullRowMapperBeforeOpeningConnection() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("rowMapper must not be null");

        verify(dataSource, never()).getConnection();
    }

    @Test
    void queryRejectsNullSetterBeforeOpeningConnection() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.query(
                "select account from users where id = ?",
                row -> row.getString("account"),
                (PreparedStatementSetter) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("setter must not be null");

        verify(dataSource, never()).getConnection();
    }

    @Test
    void queryForObjectRejectsNullRowMapperBeforeOpeningConnection() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?", null, 42L))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("rowMapper must not be null");

        verify(dataSource, never()).getConnection();
    }

    @Test
    void queryForObjectRejectsNullSetterBeforeOpeningConnection() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?",
                row -> row.getString("account"),
                (PreparedStatementSetter) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("setter must not be null");

        verify(dataSource, never()).getConnection();
    }

    @Test
    void updateRejectsNullArgumentArrayBeforeOpeningConnection() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "update users set email = ?", (Object[]) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("args must not be null");

        verify(dataSource, never()).getConnection();
    }

    @Test
    void queryRejectsNullArgumentArrayBeforeOpeningConnection() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.query(
                "select account from users where id = ?",
                row -> row.getString("account"),
                (Object[]) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("args must not be null");

        verify(dataSource, never()).getConnection();
    }

    @Test
    void queryForObjectRejectsNullArgumentArrayBeforeOpeningConnection() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?",
                row -> row.getString("account"),
                (Object[]) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("args must not be null");

        verify(dataSource, never()).getConnection();
    }

    @Test
    void updateWithSingleNullValueBindsOneParameter() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int updatedRows = jdbcTemplate.update("update users set email = ?", (Object) null);

        assertThat(updatedRows).isEqualTo(1);
        verify(statement).setObject(1, null);
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

    @Test
    void updateWithConnectionAndVarargsLeavesConnectionOpen() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int updatedRows = jdbcTemplate.update(
                connection, "update users set email = ? where id = ?", null, 42L);

        assertThat(updatedRows).isEqualTo(1);
        verify(statement).setObject(1, null);
        verify(statement).setObject(2, 42L);
        verify(statement).close();
        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void updateWithConnectionAndSetterLeavesConnectionOpen() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int updatedRows = jdbcTemplate.update(
                connection, "update users set email = ? where id = ?", preparedStatement -> {
                    preparedStatement.setNull(1, Types.VARCHAR);
                    preparedStatement.setLong(2, 42L);
                });

        assertThat(updatedRows).isEqualTo(1);
        verify(statement).setNull(1, Types.VARCHAR);
        verify(statement).setLong(2, 42L);
        verify(statement).close();
        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void queryWithConnectionReturnsRowsAndLeavesConnectionOpen() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "gugu2");

        final List<String> accounts = jdbcTemplate.query(
                connection, "select account from users where account like ?",
                row -> row.getString("account"), "gugu%");

        assertThat(accounts).containsExactly("gugu", "gugu2");
        verify(statement).setObject(1, "gugu%");
        verify(resultSet).close();
        verify(statement).close();
        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void queryForObjectWithConnectionReturnsNullWhenNoRowsAreReturned() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final String account = jdbcTemplate.queryForObject(
                connection, "select account from users where id = ?",
                row -> row.getString("account"), 42L);

        assertThat(account).isNull();
        verify(statement).setObject(1, 42L);
        verify(resultSet).close();
        verify(statement).close();
        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void queryForObjectWithConnectionAndSetterReturnsObjectAndLeavesConnectionOpen() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String account = jdbcTemplate.queryForObject(
                connection, "select account from users where id = ?",
                row -> row.getString("account"),
                preparedStatement -> preparedStatement.setLong(1, 42L));

        assertThat(account).isEqualTo("gugu");
        verify(statement).setLong(1, 42L);
        verify(resultSet).close();
        verify(statement).close();
        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void queryForObjectWithConnectionThrowsWhenMultipleRowsAreReturned() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "another-user");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                connection, "select account from users", row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Expected at most 1 result, but found 2");

        verify(resultSet).close();
        verify(statement).close();
        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void updateWithConnectionLeavesConnectionOpenWhenUpdateFails() throws SQLException {
        final SQLException failure = new SQLException("Update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(
                connection, "update users set email = ?", "gugu@example.com"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement).close();
        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void updateWithConnectionLeavesConnectionOpenWhenSetterFails() throws SQLException {
        final SQLException failure = new SQLException("Parameter binding failed");
        final PreparedStatementSetter setter = preparedStatement -> {
            throw failure;
        };

        assertThatThrownBy(() -> jdbcTemplate.update(
                connection, "update users set email = ?", setter))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void queryWithConnectionLeavesConnectionOpenWhenMappingFails() throws SQLException {
        final ResultSet resultSet = mock(ResultSet.class);
        final IllegalStateException failure = new IllegalStateException("Mapping failed");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        final RowMapper<String> rowMapper = row -> {
            throw failure;
        };

        assertThatThrownBy(() -> jdbcTemplate.query(connection, "select account from users", rowMapper))
                .isSameAs(failure);

        verify(resultSet).close();
        verify(statement).close();
        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void updateWithConnectionLeavesConnectionOpenWhenStatementCreationFails() throws SQLException {
        final SQLException failure = new SQLException("Statement creation failed");
        when(connection.prepareStatement(anyString())).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, "invalid sql"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyConnectionRemainsCallerManaged();
    }

    @Test
    void reusesBoundConnectionAcrossUpdatesWithoutClosingIt() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            jdbcTemplate.update("update users set password = ?", "changed");
            jdbcTemplate.update("insert into user_history (user_id) values (?)", 1L);

            verifyConnectionRemainsCallerManaged();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void keepsBoundConnectionOpenWhenSqlFails() throws SQLException {
        final var failure = new SQLException("Update failed");
        when(statement.executeUpdate()).thenThrow(failure);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThatThrownBy(() -> jdbcTemplate.update("update users set password = ?", "changed"))
                    .isInstanceOf(DataAccessException.class).hasCause(failure);
            verify(statement).close();
            verifyConnectionRemainsCallerManaged();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void preservesSqlFailureWhenConnectionCloseAlsoFails() throws SQLException {
        final var failure = new SQLException("Update failed");
        final var closeFailure = new SQLException("Close failed");
        when(statement.executeUpdate()).thenThrow(failure);
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("update users set password = ?", "changed"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure)
                .satisfies(thrown -> assertThat(thrown.getSuppressed())
                        .extracting(Throwable::getCause).containsExactly(closeFailure));
    }

    private void verifyConnectionRemainsCallerManaged() throws SQLException {
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection, never()).setAutoCommit(anyBoolean());
    }
}
