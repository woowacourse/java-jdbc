package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

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
