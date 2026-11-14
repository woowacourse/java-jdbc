package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("인자를 순서대로 설정하고 변경된 행 수를 반환한다")
    @Test
    void updateBindsParametersAndReturnsAffectedRows() throws SQLException {
        final var sql = "update users set email = ?, password = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(sql, "gugu@example.com", null, 1L);

        assertThat(affectedRows).isEqualTo(1);
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, "gugu@example.com");
        verify(statement).setObject(2, null);
        verify(statement).setObject(3, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @DisplayName("변경 쿼리가 실패하면 자원을 닫고 원인 예외를 보존한다")
    @Test
    void updateClosesResourcesAndPreservesSqlException() throws SQLException {
        final var cause = new SQLException("쿼리 실행에 실패했습니다.");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @DisplayName("조회 인자를 설정하고 모든 행을 객체로 변환한다")
    @Test
    void queryBindsParametersAndMapsEveryRow() throws SQLException {
        final var sql = "select account from users where id > ?";
        final var resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        final var accounts = jdbcTemplate.query(sql, row -> row.getString("account"), 0L);

        assertThat(accounts).containsExactly("gugu", "other");
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, 0L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
    @Test
    void queryReturnsEmptyListWhenNoRowsExist() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);

        final var accounts = jdbcTemplate.query("select account from users", row -> row.getString("account"));

        assertThat(accounts).isEmpty();
        verify(resultSet, never()).getString(anyString());
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @DisplayName("조회 결과 변환 중 SQL 예외가 발생하면 자원을 닫는다")
    @Test
    void queryClosesResourcesWhenMappingFails() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        final var cause = new SQLException("조회 결과 변환에 실패했습니다.");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", row -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @DisplayName("조회 쿼리 실행이 실패하면 자원을 닫는다")
    @Test
    void queryClosesResourcesWhenExecutionFails() throws SQLException {
        final var cause = new SQLException("쿼리 실행에 실패했습니다.");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @DisplayName("쿼리 준비가 실패하면 연결을 닫는다")
    @Test
    void updateClosesConnectionWhenStatementCreationFails() throws SQLException {
        final var sql = "delete from users";
        final var cause = new SQLException("쿼리 준비에 실패했습니다.");
        when(connection.prepareStatement(sql)).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
    }

    @DisplayName("조회 인자 설정이 실패하면 자원을 닫고 쿼리를 실행하지 않는다")
    @Test
    void queryClosesResourcesWhenBindingFails() throws SQLException {
        final var cause = new SQLException("쿼리 인자 설정에 실패했습니다.");
        doThrow(cause).when(statement).setObject(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select account from users where id = ?", row -> row.getString("account"), 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeQuery();
        verify(statement).close();
        verify(connection).close();
    }

    @DisplayName("조회 결과 변환 중 런타임 예외가 발생해도 자원을 닫는다")
    @Test
    void queryClosesResourcesWhenMapperThrowsUncheckedException() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        final var cause = new IllegalArgumentException("조회 결과 변환에 실패했습니다.");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", row -> {
            throw cause;
        })).isSameAs(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @DisplayName("외부 연결로 변경 쿼리를 실행해도 연결을 닫거나 트랜잭션을 종료하지 않는다")
    @Test
    void updateWithExternalConnectionLeavesConnectionOpen() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(connection, "update users set password = ? where id = ?",
                "new-password", 1L);

        assertThat(affectedRows).isEqualTo(1);
        verify(statement).setObject(1, "new-password");
        verify(statement).setObject(2, 1L);
        verify(statement).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @DisplayName("외부 연결로 조회하면 Statement와 ResultSet만 닫는다")
    @Test
    void queryWithExternalConnectionClosesOnlyQueryResources() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var accounts = jdbcTemplate.query(connection, "select account from users where id = ?",
                row -> row.getString("account"), 1L);

        assertThat(accounts).containsExactly("gugu");
        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @DisplayName("외부 연결로 실행한 변경 쿼리가 실패해도 연결은 닫지 않는다")
    @Test
    void updateWithExternalConnectionLeavesConnectionOpenOnFailure() throws SQLException {
        final var cause = new SQLException("쿼리 실행에 실패했습니다.");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, "delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection, never()).close();
    }

    @DisplayName("외부 연결에서 조회 결과 변환이 실패해도 연결은 닫지 않는다")
    @Test
    void queryWithExternalConnectionLeavesConnectionOpenWhenMappingFails() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        final var cause = new SQLException("조회 결과 변환에 실패했습니다.");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(connection, "select account from users", row -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
    }
}
