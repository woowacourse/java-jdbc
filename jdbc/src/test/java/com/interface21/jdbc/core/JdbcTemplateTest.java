package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private final String sql = "select account from users where id = ? and account = ?";

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class, withSettings().mockMaker(MockMakers.PROXY));
        connection = mock(Connection.class, withSettings().mockMaker(MockMakers.PROXY));
        statement = mock(PreparedStatement.class, withSettings().mockMaker(MockMakers.PROXY));
        resultSet = mock(ResultSet.class, withSettings().mockMaker(MockMakers.PROXY));
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void mapsRowWithParametersOfDifferentTypes() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        String account = jdbcTemplate.queryForObject(sql, rs -> rs.getString("account"), 1L, "gugu");

        assertThat(account).isEqualTo("gugu");
        verify(statement).setObject(1, 1L);
        verify(statement).setObject(2, "gugu");
        verifyResourcesClosed();
    }

    @Test
    void returnsNullWithoutMappingWhenNoRowExists() throws SQLException {
        RowMapper<?> mapper = mock(RowMapper.class, withSettings().mockMaker(MockMakers.PROXY));

        assertThat(jdbcTemplate.queryForObject(sql, mapper, 1L, "gugu")).isNull();

        verifyNoInteractions(mapper);
        verifyResourcesClosed();
    }

    @Test
    void closesResourcesAndPreservesCauseWhenMappingFails() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        SQLException failure = new SQLException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rs -> {
            throw failure;
        }, 1L, "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyResourcesClosed();
    }

    @Test
    void queryMapsEveryRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "ever");

        assertThat(jdbcTemplate.query(sql, rs -> rs.getString("account"), 1L, "gugu"))
                .containsExactly("gugu", "ever");
        verifyResourcesClosed();
    }

    @Test
    void queryReturnsEmptyListWhenNoRowsExist() throws SQLException {
        assertThat(jdbcTemplate.query(sql, rs -> rs.getString("account"), 1L, "gugu"))
                .isEmpty();
        verifyResourcesClosed();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rs -> "user", 1L, "gugu"))
                .isInstanceOf(IllegalStateException.class);
        verifyResourcesClosed();
    }

    @Test
    void updateReturnsAffectedRowCount() throws SQLException {
        when(statement.executeUpdate()).thenReturn(2);

        assertThat(jdbcTemplate.update(sql, 1L, "gugu")).isEqualTo(2);

        verify(statement).setObject(1, 1L);
        verify(statement).setObject(2, "gugu");
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void closesStatementAndConnectionWhenBindingFails() throws SQLException {
        SQLException failure = new SQLException("binding failed");
        doThrow(failure).when(statement).setObject(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.update(sql, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateWithSetterBindsBeforeExecution() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        int affectedRows = jdbcTemplate.update(sql, pstmt -> {
            pstmt.setLong(1, 1L);
            pstmt.setString(2, "gugu");
        });

        assertThat(affectedRows).isEqualTo(1);
        var order = inOrder(statement);
        order.verify(statement).setLong(1, 1L);
        order.verify(statement).setString(2, "gugu");
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryWithSetterBindsBeforeExecutionAndMapsRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "ever");

        var accounts = jdbcTemplate.query(sql, rs -> rs.getString("account"), pstmt -> {
            pstmt.setLong(1, 1L);
            pstmt.setString(2, "gugu");
        });

        assertThat(accounts).containsExactly("gugu", "ever");
        var order = inOrder(statement);
        order.verify(statement).setLong(1, 1L);
        order.verify(statement).setString(2, "gugu");
        order.verify(statement).executeQuery();
        verifyResourcesClosed();
    }

    @Test
    void updateClosesResourcesWhenSetterFails() throws SQLException {
        SQLException failure = new SQLException("setter failed");

        assertThatThrownBy(() -> jdbcTemplate.update(sql, pstmt -> {
            throw failure;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryClosesResourcesWhenSetterFails() throws SQLException {
        SQLException failure = new SQLException("setter failed");

        assertThatThrownBy(() -> jdbcTemplate.query(sql, rs -> rs.getString("account"), pstmt -> {
            throw failure;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement, never()).executeQuery();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateReusesBoundConnectionWithoutClosingIt() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            jdbcTemplate.update(sql, 1L, "gugu");

            verify(dataSource, never()).getConnection();
            verify(statement).executeUpdate();
            verify(statement).close();
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void boundConnectionIsInvisibleToAnotherThread() throws Exception {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            final var otherThread = executor.submit(() -> TransactionSynchronizationManager.getResource(dataSource));

            assertThat(otherThread.get(5, TimeUnit.SECONDS)).isNull();
            assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void closeFailureDoesNotReplaceSqlFailure() throws SQLException {
        final var sqlFailure = new SQLException("update failed");
        final var closeFailure = new SQLException("close failed");
        when(statement.executeUpdate()).thenThrow(sqlFailure);
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update(sql, 1L, "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(sqlFailure)
                .satisfies(failure -> assertThat(failure.getSuppressed()).singleElement()
                        .isInstanceOf(CannotGetJdbcConnectionException.class)
                        .satisfies(suppressed -> assertThat(suppressed).hasCause(closeFailure)));
    }

    private void verifyResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
