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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateSetterTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final String sql = "select account from users where id = ?";
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    private final PreparedStatementSetter setter = statement -> statement.setLong(1, 1L);
    private final RowMapper<String> rowMapper = resultSet -> resultSet.getString("account");

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    @DisplayName("매핑한 결과를 반환하고 모든 자원을 해제한다")
    void query() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.query(sql, setter, rowMapper)).isEqualTo(List.of("gugu"));

        verify(statement).setLong(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 실행이 실패하면 SQL과 원인을 보존하고 자원을 해제한다")
    void query_failure() throws SQLException {
        final var failure = new SQLException("조회 실행 실패");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(sql, setter, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("매핑이 실패하면 SQL과 원인을 보존하고 모든 자원을 해제한다")
    void mapping_failure() throws SQLException {
        final var failure = new SQLException("결과 매핑 실패");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(sql, setter, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("매핑 중 런타임 예외가 발생해도 같은 예외를 던지고 모든 자원을 해제한다")
    void mapping_runtime_failure() throws SQLException {
        final var failure = new IllegalArgumentException("결과 변환 실패");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(sql, setter, rowMapper)).isSameAs(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
    void query_with_no_rows() {
        assertThat(jdbcTemplate.query(sql, setter, rowMapper)).isEqualTo(List.of());
    }

    @Test
    @DisplayName("조회한 여러 행을 순서대로 매핑한다")
    void query_with_multiple_rows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "brown");

        assertThat(jdbcTemplate.query(sql, setter, rowMapper)).isEqualTo(List.of("gugu", "brown"));
    }

    @Test
    @DisplayName("조회 값을 설정하다 실패하면 SQL과 원인을 보존하고 자원을 해제한다")
    void setter_failure() throws SQLException {
        final var failure = new SQLException("조회 값 설정 실패");
        doThrow(failure).when(statement).setLong(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.query(sql, setter, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("전달한 setter로 값을 설정하고 변경 건수를 반환한다")
    void update_with_setter() throws SQLException {
        when(statement.executeUpdate()).thenReturn(2);

        assertThat(jdbcTemplate.update("update users set id = ?", setter)).isEqualTo(2);

        verify(statement).setLong(1, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("매개변수 없는 수정은 변경 건수가 0이어도 그대로 반환한다")
    void update_with_no_parameters() throws SQLException {
        assertThat(jdbcTemplate.update("delete from users")).isZero();

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("수정 setter의 SQL 예외는 원인을 보존하고 자원을 해제한다")
    void update_setter_sql_failure() throws SQLException {
        final var failure = new SQLException("수정 값 설정 실패");
        doThrow(failure).when(statement).setLong(1, 1L);

        final var updateSql = "update users set id = ?";
        assertThatThrownBy(() -> jdbcTemplate.update(updateSql, setter))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(updateSql)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("수정 setter의 런타임 예외는 그대로 전달하고 자원을 해제한다")
    void update_setter_runtime_failure() throws SQLException {
        final var failure = new IllegalArgumentException("수정 값 확인 실패");
        doThrow(failure).when(statement).setLong(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.update("update users set id = ?", setter)).isSameAs(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단일 null 값은 Object로 명시해 바인딩한다")
    void update_with_single_null() throws SQLException {
        jdbcTemplate.update("update users set account = ?", (Object) null);

        verify(statement).setObject(1, null);
    }

    @Test
    @DisplayName("null 하나를 담은 배열도 수정 매개변수로 바인딩한다")
    void update_with_single_null_array() throws SQLException {
        jdbcTemplate.update("update users set account = ?", new Object[]{null});

        verify(statement).setObject(1, null);
    }

    @Test
    @DisplayName("setter를 받는 단건 조회도 결과가 없으면 null을 반환한다")
    void query_for_object_with_no_rows() {
        assertThat(jdbcTemplate.queryForObject(sql, setter, rowMapper)).isNull();
    }

    @Test
    @DisplayName("setter를 받는 단건 조회는 매핑한 결과를 반환한다")
    void query_for_object() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.queryForObject(sql, setter, rowMapper)).isEqualTo("gugu");

        verify(statement).setLong(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("setter를 받는 단건 조회가 여러 건이면 SQL과 정확한 건수를 담은 예외를 던진다")
    void query_for_object_with_multiple_rows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, setter, rowMapper))
                .isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessageContaining("found 3")
                .hasMessageContaining(sql);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
