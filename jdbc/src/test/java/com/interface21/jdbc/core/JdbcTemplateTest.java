package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    @DisplayName("수정 쿼리에 null을 포함한 매개변수를 순서대로 바인딩하고 변경 건수를 반환한다")
    void update_binds_parameters() throws SQLException {
        final var sql = "update users set account = ?, email = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(2);

        final var count = jdbcTemplate.update(sql, "gugu", null, 1L);

        assertThat(count).isEqualTo(2);
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, null);
        verify(statement).setObject(3, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회한 모든 행을 매핑하고 자원을 해제한다")
    void query_maps_rows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "brown");

        final var accounts = jdbcTemplate.query("select account from users where id > ?",
                row -> row.getString("account"), 0L);

        assertThat(accounts).containsExactly("gugu", "brown");
        verify(statement).setObject(1, 0L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
    void query_with_no_rows() {
        final var accounts = jdbcTemplate.query("select account from users", row -> row.getString("account"));

        assertThat(accounts).isEmpty();
    }

    @Test
    @DisplayName("조회 결과가 한 건이면 매핑한 객체를 반환한다")
    void query_for_object() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);

        final var id = jdbcTemplate.queryForObject("select id from users where account = ?",
                row -> row.getLong("id"), "gugu");

        assertThat(id).isEqualTo(1L);
        verify(statement).setObject(1, "gugu");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 null을 반환한다")
    void query_for_object_with_no_rows() throws SQLException {
        final var id = jdbcTemplate.queryForObject("select id from users", row -> row.getLong("id"));

        assertThat(id).isNull();
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회에 두 번째 행이 있으면 추가 행을 매핑하지 않고 예외를 던진다")
    void query_for_object_with_multiple_rows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, true, false);
        final var sql = "select id from users";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, row -> row.getLong("id")))
                .isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessageContaining("at most one row")
                .hasMessageContaining("at least 2")
                .hasMessageContaining(sql);
        verify(resultSet, times(2)).next();
        verify(resultSet).getLong("id");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("커넥션을 얻지 못하면 실행한 SQL과 원인을 담은 예외를 던진다")
    void connection_failure() throws SQLException {
        final var failure = new SQLException("Connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);
        final var sql = "delete from users";

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
    }

    @Test
    @DisplayName("쿼리를 준비하다 실패해도 커넥션을 해제한다")
    void prepare_statement_failure() throws SQLException {
        final var sql = "delete from users";
        final var failure = new SQLException("Invalid SQL");
        when(connection.prepareStatement(sql)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(connection).close();
    }

    @Test
    @DisplayName("매개변수를 바인딩하다 실패해도 자원을 해제한다")
    void parameter_binding_failure() throws SQLException {
        final var failure = new SQLException("Unsupported parameter");
        doThrow(failure).when(statement).setObject(1, "gugu");

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users where account = ?", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("수정 쿼리가 실패하면 자원을 해제하고 원인을 보존한다")
    void update_failure() throws SQLException {
        final var failure = new SQLException("Constraint violation");
        when(statement.executeUpdate()).thenThrow(failure);
        final var sql = "insert into users (account) values (?)";

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 쿼리가 실패하면 자원을 해제하고 원인을 보존한다")
    void query_failure() throws SQLException {
        final var failure = new SQLException("Invalid query");
        when(statement.executeQuery()).thenThrow(failure);
        final var sql = "select id from missing_table";

        assertThatThrownBy(() -> jdbcTemplate.query(sql, row -> row.getLong("id")))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과를 순회하다 실패하면 부분 결과를 반환하지 않고 자원을 해제한다")
    void query_iteration_failure() throws SQLException {
        final var failure = new SQLException("Cursor unavailable");
        when(resultSet.next()).thenReturn(true).thenThrow(failure);
        final var sql = "select id from users";

        assertThatThrownBy(() -> jdbcTemplate.query(sql, row -> row.getLong("id")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회의 두 번째 행을 확인하다 실패해도 원인을 보존하고 자원을 해제한다")
    void query_for_object_iteration_failure() throws SQLException {
        final var failure = new SQLException("Cursor unavailable");
        when(resultSet.next()).thenReturn(true).thenThrow(failure);
        final var sql = "select id from users";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, row -> row.getLong("id")))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회의 행 매핑이 실패해도 SQL 예외 원인을 보존하고 자원을 해제한다")
    void query_for_object_row_mapping_failure() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        final var failure = new SQLException("Column unavailable");
        when(resultSet.getString("account")).thenThrow(failure);
        final var sql = "select account from users";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회 중 런타임 예외가 발생해도 모든 자원을 해제한다")
    void query_for_object_runtime_failure() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        final var failure = new IllegalArgumentException("Cannot map row");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select id from users", row -> {
                    throw failure;
                }))
                .isSameAs(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("행을 매핑하다 SQL 예외가 발생해도 모든 자원을 해제하고 원인을 보존한다")
    void row_mapping_sql_failure() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        final var failure = new SQLException("Column unavailable");
        when(resultSet.getString("account")).thenThrow(failure);
        final var sql = "select account from users";

        assertThatThrownBy(() -> jdbcTemplate.query(sql, row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("행을 매핑하다 런타임 예외가 발생해도 모든 자원을 해제한다")
    void row_mapping_runtime_failure() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        final var failure = new IllegalArgumentException("Cannot map row");

        assertThatThrownBy(() -> jdbcTemplate.query("select id from users", row -> {
                    throw failure;
                }))
                .isSameAs(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("자원 해제 중 발생한 예외가 기존 SQL 예외를 덮어쓰지 않는다")
    void resource_cleanup_failure() throws SQLException {
        final var failure = new SQLException("Execution failed");
        final var closeFailure = new SQLException("Close failed");
        when(statement.executeUpdate()).thenThrow(failure);
        doThrow(closeFailure).when(statement).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);
        assertThat(failure.getSuppressed()).containsExactly(closeFailure);
        verify(connection).close();
    }
}
