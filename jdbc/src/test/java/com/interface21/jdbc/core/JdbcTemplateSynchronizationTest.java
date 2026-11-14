package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateSynchronizationTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    @DisplayName("바인딩된 커넥션으로 조회한 뒤 Statement와 ResultSet만 닫는다")
    void query_with_bound_connection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);

        assertThat(jdbcTemplate.query("select id from users", row -> row.getLong("id"))).isEmpty();

        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("바인딩된 커넥션에서 SQL이 실패해도 커넥션을 닫지 않는다")
    void failure_with_bound_connection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var failure = new SQLException("Update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);
        verify(statement).close();
        verify(connection, never()).close();
    }

    @Test
    @DisplayName("일반 커넥션 종료가 실패하면 SQL과 원인을 보존한다")
    void close_failure() throws SQLException {
        final var failure = new SQLException("Close failed");
        doThrow(failure).when(connection).close();
        final var sql = "delete from users";

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
    }

    @Test
    @DisplayName("SQL 실패 뒤 커넥션 종료도 실패하면 처음 예외에 종료 실패를 보존한다")
    void execution_and_close_failure() throws SQLException {
        final var failure = new SQLException("Update failed");
        final var closeFailure = new SQLException("Close failed");
        when(statement.executeUpdate()).thenThrow(failure);
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure)
                .hasSuppressedException(closeFailure);
    }
}
