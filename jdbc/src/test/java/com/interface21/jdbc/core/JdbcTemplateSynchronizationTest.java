package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateSynchronizationTest {

    private DataSource dataSource;
    private Connection connection;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void clearResource() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    void queryAndUpdateReuseBoundConnectionUntilTransactionReleasesIt() throws SQLException {
        final var queryStatement = mock(PreparedStatement.class);
        final var updateStatement = mock(PreparedStatement.class);
        final var resultSet = mock(ResultSet.class);
        when(connection.prepareStatement("select name from users")).thenReturn(queryStatement);
        when(connection.prepareStatement("update users set name = ?")).thenReturn(updateStatement);
        when(queryStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("eian");
        when(updateStatement.executeUpdate()).thenReturn(1);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        final String name = jdbcTemplate.queryForObject("select name from users", row -> row.getString("name"));
        assertThat(name).isEqualTo("eian");
        assertThat(jdbcTemplate.update("update users set name = ?", "new-name")).isEqualTo(1);

        verifyNoInteractions(dataSource);
        verify(resultSet).close();
        verify(queryStatement).close();
        verify(updateStatement).close();
        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);

        TransactionSynchronizationManager.unbindResource(dataSource);
        DataSourceUtils.releaseConnection(connection, dataSource);
        verify(connection).close();
    }

    @Test
    void failureClosesStatementButKeepsBoundConnectionAvailableForRollback() throws SQLException {
        final var statement = mock(PreparedStatement.class);
        final var failure = new SQLException("update failed");
        when(connection.prepareStatement("update users set name = ?")).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(failure);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> jdbcTemplate.update("update users set name = ?", "new-name"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement).close();
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verifyNoInteractions(dataSource);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void standaloneCallsCloseTheirConnectionsWithoutLeavingThreadResources() throws SQLException {
        final var secondConnection = mock(Connection.class);
        final var firstStatement = mock(PreparedStatement.class);
        final var secondStatement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection, secondConnection);
        when(connection.prepareStatement("delete from users")).thenReturn(firstStatement);
        when(secondConnection.prepareStatement("delete from users")).thenReturn(secondStatement);

        jdbcTemplate.update("delete from users");
        jdbcTemplate.update("delete from users");

        verify(dataSource, times(2)).getConnection();
        verify(firstStatement).close();
        verify(secondStatement).close();
        verify(connection).close();
        verify(secondConnection).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }
}
