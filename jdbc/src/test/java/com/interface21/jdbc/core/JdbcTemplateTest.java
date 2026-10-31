package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    @Test
    @DisplayName("트랜잭션의 여러 SQL은 같은 연결을 쓰고 연결을 닫지 않는다")
    void reusesTransactionConnectionAcrossStatements() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var template = new JdbcTemplate(dataSource);
        final var sql = "update users set password = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        try {
            template.update(sql, "first", 1L);
            template.update(sql, "second", 1L);

            verify(dataSource, never()).getConnection();
            verify(connection, times(2)).prepareStatement(sql);
            verify(connection, never()).close();
            verify(statement, times(2)).close();
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
        verify(connection).close();
    }

    @Test
    @DisplayName("SQL 실행과 연결 닫기가 모두 실패해도 SQL 오류를 유지한다")
    void preservesSqlFailureWhenCloseAlsoFails() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var sqlFailure = new SQLException("SQL failed");
        final var closeFailure = new SQLException("Close failed");
        final var sql = "update users set password = ?";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(sqlFailure);
        doThrow(closeFailure).when(connection).close();

        final var exception = assertThrows(DataAccessException.class,
                () -> new JdbcTemplate(dataSource).update(sql, "new-password"));

        assertThat(exception).hasCause(sqlFailure);
        assertThat(exception.getSuppressed()).hasSize(1);
        assertThat(exception.getSuppressed()[0]).hasCause(closeFailure);
    }

    @Test
    @DisplayName("조회한 행을 순서대로 매핑하고 모든 자원을 닫는다")
    void queryMapsRowsAndClosesResources() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var resultSet = mock(ResultSet.class);
        final var sql = "select name from users where id > ?";
        final RowMapper<String> rowMapper = rs -> rs.getString(1);
        final var template = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString(1)).thenReturn("first", "second");

        final List<String> result = template.query(sql, rowMapper, 1L);

        assertThat(result).containsExactly("first", "second");
        final var order = inOrder(statement);
        order.verify(statement).setObject(1, 1L);
        order.verify(statement).executeQuery();
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록을 돌려준다")
    void queryReturnsEmptyListWhenNoRows() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var resultSet = mock(ResultSet.class);
        final var sql = "select name from users";
        final var mappedRows = new AtomicInteger();
        final RowMapper<String> rowMapper = rs -> {
            mappedRows.incrementAndGet();
            return rs.getString(1);
        };
        final var template = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final List<String> result = template.query(sql, rowMapper);

        assertThat(result).isEmpty();
        assertThat(mappedRows.get()).isZero();
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 null을 돌려주고 자원을 닫는다")
    void queryForObjectReturnsNullWhenNoRows() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var resultSet = mock(ResultSet.class);
        final var sql = "select name from users";
        final var mappedRows = new AtomicInteger();
        final RowMapper<String> rowMapper = rs -> {
            mappedRows.incrementAndGet();
            return rs.getString(1);
        };
        final var template = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final var result = template.queryForObject(sql, rowMapper);

        assertThat(result).isNull();
        assertThat(mappedRows.get()).isZero();
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회 결과가 한 건이면 매핑한 값을 돌려주고 자원을 닫는다")
    void queryForObjectReturnsMappedRowWhenOneRow() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var resultSet = mock(ResultSet.class);
        final var sql = "select name from users";
        final var template = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString(1)).thenReturn("only-user");

        final var result = template.queryForObject(sql, rs -> rs.getString(1));

        assertThat(result).isEqualTo("only-user");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회 결과가 여러 건이면 예외를 던지고 자원을 닫는다")
    void queryForObjectThrowsWhenMultipleRows() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var resultSet = mock(ResultSet.class);
        final var sql = "select name from users";
        final var mappedRows = new AtomicInteger();
        final RowMapper<String> rowMapper = rs -> {
            mappedRows.incrementAndGet();
            return rs.getString(1);
        };
        final var template = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true);
        when(resultSet.getString(1)).thenReturn("first-user");

        final var exception = assertThrows(DataAccessException.class,
                () -> template.queryForObject(sql, rowMapper));

        assertThat(exception).hasMessageContaining("multiple rows");
        assertThat(mappedRows.get()).isOne();
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("값을 순서대로 SQL에 넣고 변경된 행 수를 돌려준다")
    void updateBindsValuesAndReturnsUpdatedRowCount() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var sql = "update users set password = ? where id = ?";
        final var template = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        final var updatedRows = template.update(sql, "new-password", 1L);

        assertThat(updatedRows).isEqualTo(1);
        final var order = inOrder(statement);
        order.verify(statement).setObject(1, "new-password");
        order.verify(statement).setObject(2, 1L);
        order.verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("SQL 실행에 실패하면 원인 오류를 담고 자원을 닫는다")
    void updateWrapsSqlFailureAndClosesResources() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var sql = "update users set password = ? where id = ?";
        final var sqlException = new SQLException("SQL execution failed");
        final var template = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(sqlException);

        final var exception = assertThrows(DataAccessException.class,
                () -> template.update(sql, "new-password", 1L));

        assertThat(exception.getCause()).isSameAs(sqlException);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과를 읽다가 실패해도 원인 오류를 담고 모든 자원을 닫는다")
    void queryWrapsMappingFailureAndClosesResources() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var statement = mock(PreparedStatement.class);
        final var resultSet = mock(ResultSet.class);
        final var sqlException = new SQLException("Mapping failed");
        final var sql = "select name from users";
        final var template = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        final var exception = assertThrows(DataAccessException.class,
                () -> template.query(sql, rs -> {
                    throw sqlException;
                }));

        assertThat(exception.getCause()).isSameAs(sqlException);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("DB 연결을 얻지 못하면 연결 예외를 던진다")
    void updateThrowsConnectionExceptionWhenConnectionCannotBeObtained() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var sqlException = new SQLException("Connection failed");
        final var template = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenThrow(sqlException);

        final var exception = assertThrows(CannotGetJdbcConnectionException.class,
                () -> template.update("update users set password = ?", "new-password"));

        assertThat(exception.getCause()).isSameAs(sqlException);
    }
}
