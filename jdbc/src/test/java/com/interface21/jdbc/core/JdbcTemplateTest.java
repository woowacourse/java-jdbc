package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    @Test
    void executesOnTheBoundConnectionWithoutClosingIt() throws SQLException {
        var dataSource = mock(DataSource.class);
        var connection = mock(Connection.class);
        var statement = mock(PreparedStatement.class);
        var sql = "update users set password = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            assertThat(new JdbcTemplate(dataSource).update(sql, "newPassword", 1L)).isEqualTo(1);

            verifyNoInteractions(dataSource);
            verify(statement).close();
            verify(connection, never()).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void keepsTheSqlFailureWhenClosingAnUnboundConnectionAlsoFails() throws SQLException {
        var dataSource = mock(DataSource.class);
        var connection = mock(Connection.class);
        var statement = mock(PreparedStatement.class);
        var sql = "update users set password = ? where id = ?";
        var executionFailure = new SQLException("execution failed");
        var closeFailure = new SQLException("close failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(executionFailure);
        doThrow(closeFailure).when(connection).close();

        var failure = assertThrows(DataAccessException.class,
                () -> new JdbcTemplate(dataSource).update(sql, "newPassword", 1L));

        assertThat(failure).hasCause(executionFailure);
        assertThat(failure.getSuppressed()).singleElement().satisfies(
                suppressed -> assertThat(suppressed).hasCause(closeFailure));
        verify(statement).close();
    }
}
