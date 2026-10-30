package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DuplicateKeyException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "update users set password = ? where id = ?";
    private static final String QUERY = "select email from users where id > ?";
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet rows = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(connection.prepareStatement(QUERY)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(rows);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void updateBindsBeforeExecutionAndClosesResources() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final var count = jdbcTemplate.update(SQL, pstmt -> {
            pstmt.setString(1, "changed");
            pstmt.setLong(2, 1L);
        });

        assertThat(count).isEqualTo(1);
        final var order = inOrder(statement, connection);
        order.verify(statement).setString(1, "changed");
        order.verify(statement).setLong(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void updateWithVarargsBindsEachValueInOrder() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final var count = jdbcTemplate.update(SQL, "changed", 1L);

        assertThat(count).isEqualTo(1);
        final var order = inOrder(statement, connection);
        order.verify(statement).setObject(1, "changed");
        order.verify(statement).setObject(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void queryWithoutArgsBindsNothing() throws SQLException {
        when(rows.next()).thenReturn(true, false);
        when(rows.getString("email")).thenReturn("first@example.com");

        final var result = jdbcTemplate.query(QUERY, rs -> rs.getString("email"));

        assertThat(result).containsExactly("first@example.com");
        verify(statement, never()).setObject(anyInt(), any());
    }

    @Test
    void queryForObjectWithVarargsBindsBeforeQuery() throws SQLException {
        when(rows.next()).thenReturn(true, false);
        when(rows.getString("email")).thenReturn("first@example.com");

        final var result = jdbcTemplate.queryForObject(QUERY, rs -> rs.getString("email"), 7L);

        assertThat(result).isEqualTo("first@example.com");
        final var order = inOrder(statement);
        order.verify(statement).setObject(1, 7L);
        order.verify(statement).executeQuery();
    }

    @Test
    void usesBoundConnectionAndLeavesItOpen() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        when(statement.executeUpdate()).thenReturn(1);

        final var count = jdbcTemplate.update(SQL, "changed", 1L);

        assertThat(count).isEqualTo(1);
        final var order = inOrder(statement);
        order.verify(statement).setObject(1, "changed");
        order.verify(statement).setObject(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void boundConnectionFailureTranslatesButDoesNotClose() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var failure = new SQLException("execution failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, 1L))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(statement).close();
        verify(connection, never()).close();
    }

    @Test
    void setterFailureClosesResourcesWithoutExecuting() throws SQLException {
        final var failure = new SQLException("binding failed");

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, pstmt -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void executionFailureClosesResourcesAndPreservesCause() throws SQLException {
        final var failure = new SQLException("execution failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, pstmt -> {}))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void executionFailureMessageNamesTheSql() throws SQLException {
        when(statement.executeUpdate()).thenThrow(new SQLException("execution failed"));

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, pstmt -> {}))
                .isInstanceOf(DataAccessException.class)
                .isNotInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining(SQL);
    }

    @Test
    void uniqueViolationBecomesDuplicateKeyException() throws SQLException {
        final var failure = new SQLException("unique index violated", "23505");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, pstmt -> {}))
                .isInstanceOf(DuplicateKeyException.class).hasCause(failure);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void statementPreparationFailureClosesConnection() throws SQLException {
        final var failure = new SQLException("preparation failed");
        when(connection.prepareStatement(SQL)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, pstmt -> {}))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).close();
    }

    @Test
    void connectionFailurePreservesCause() throws SQLException {
        final var failure = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, pstmt -> {}))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection, never()).prepareStatement(SQL);
    }

    @Test
    void queryMapsAllRowsAndClosesResources() throws SQLException {
        when(rows.next()).thenReturn(true, true, false);
        when(rows.getString("email")).thenReturn("first@example.com", "second@example.com");

        final var result = jdbcTemplate.query(QUERY, pstmt -> pstmt.setLong(1, 0L),
                rs -> rs.getString("email"));

        assertThat(result).containsExactly("first@example.com", "second@example.com");
        final var order = inOrder(statement, rows, connection);
        order.verify(statement).setLong(1, 0L);
        order.verify(statement).executeQuery();
        order.verify(rows).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void queryReturnsEmptyListWithoutMappingWhenNoRows() throws SQLException {
        when(rows.next()).thenReturn(false);

        final var result = jdbcTemplate.query(QUERY, pstmt -> {}, rs -> {
            throw new AssertionError("No row to map");
        });

        assertThat(result).isEmpty();
        verify(rows).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectReturnsSingleRow() throws SQLException {
        when(rows.next()).thenReturn(true, false);
        when(rows.getString("email")).thenReturn("first@example.com");

        final var result = jdbcTemplate.queryForObject(QUERY, pstmt -> {}, rs -> rs.getString("email"));

        assertThat(result).isEqualTo("first@example.com");
        verify(rows, times(2)).next();
        verify(rows, times(1)).getString("email");
        verify(rows).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(rows.next()).thenReturn(true, true, false);
        when(rows.getString("email")).thenReturn("first@example.com", "second@example.com");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(QUERY, pstmt -> {}, rs -> rs.getString("email")))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessageContaining("expected 1, actual 2");

        verify(rows).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectReturnsNullWhenNoRows() throws SQLException {
        when(rows.next()).thenReturn(false);

        final var result = jdbcTemplate.queryForObject(QUERY, pstmt -> {}, rs -> {
            throw new AssertionError("No row to map");
        });

        assertThat(result).isNull();
        verify(rows).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMappingFailureClosesResourcesAndPreservesCause() throws SQLException {
        when(rows.next()).thenReturn(true);
        final var failure = new SQLException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY, pstmt -> {}, rs -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(rows).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectMappingFailureClosesResourcesAndPreservesCause() throws SQLException {
        when(rows.next()).thenReturn(true);
        final var failure = new SQLException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(QUERY, pstmt -> {}, rs -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(rows).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryExecutionFailureClosesStatementAndConnection() throws SQLException {
        final var failure = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY, pstmt -> {}, rs -> rs.getString(1)))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectSetterFailureDoesNotExecute() throws SQLException {
        final var failure = new SQLException("binding failed");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(QUERY, pstmt -> {
            throw failure;
        }, rs -> rs.getString(1))).isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(statement, never()).executeQuery();
        verify(statement).close();
        verify(connection).close();
    }
}
