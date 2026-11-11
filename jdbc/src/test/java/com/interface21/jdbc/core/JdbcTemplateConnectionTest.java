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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateConnectionTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    private final String sql = "select account from users where id = ?";
    private final PreparedStatementSetter setter = statement -> statement.setLong(1, 1L);
    private final RowMapper<String> rowMapper = resultSet -> resultSet.getString("account");

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    @DisplayName("전달받은 커넥션에 null을 포함한 값을 바인딩하고 변경 건수를 반환한다")
    void update_binds_parameters() throws SQLException {
        final var updateSql = "update users set account = ?, email = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(2);

        assertThat(jdbcTemplate.update(connection, updateSql, "gugu", null, 1L)).isEqualTo(2);

        verify(connection).prepareStatement(updateSql);
        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, null);
        verify(statement).setObject(3, 1L);
        verify(statement).close();
        verify(connection, never()).close();
        verifyNoInteractions(dataSource);
    }

    @Test
    @DisplayName("외부 커넥션의 수정도 setter로 값을 설정한 뒤 실행한다")
    void update_with_setter() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update(connection, "update users set id = ?", setter)).isEqualTo(1);

        final var order = inOrder(statement);
        order.verify(statement).setLong(1, 1L);
        order.verify(statement).executeUpdate();
    }

    @Test
    @DisplayName("외부 커넥션으로 조회한 모든 행을 순서대로 매핑하고 조회 자원을 해제한다")
    void query_maps_rows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "brown");

        assertThat(jdbcTemplate.query(connection, sql, rowMapper, 1L)).containsExactly("gugu", "brown");

        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verifyNoInteractions(dataSource);
    }

    @Test
    @DisplayName("외부 커넥션의 조회도 setter로 값을 설정한 뒤 실행한다")
    void query_with_setter() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.query(connection, sql, setter, rowMapper)).containsExactly("gugu");

        final var order = inOrder(statement);
        order.verify(statement).setLong(1, 1L);
        order.verify(statement).executeQuery();
    }

    @Test
    @DisplayName("외부 커넥션의 단건 조회는 매개변수를 바인딩하고 매핑한 객체를 반환한다")
    void query_for_object() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.queryForObject(connection, sql, rowMapper, 1L)).isEqualTo("gugu");

        verify(statement).setObject(1, 1L);
    }

    @Test
    @DisplayName("외부 커넥션의 단건 조회에도 setter를 전달할 수 있다")
    void query_for_object_with_setter() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.queryForObject(connection, sql, setter, rowMapper)).isEqualTo("gugu");

        verify(statement).setLong(1, 1L);
    }

    @Test
    @DisplayName("외부 커넥션의 단건 조회 결과가 없으면 null을 반환한다")
    void query_for_object_with_no_rows() throws SQLException {
        assertThat(jdbcTemplate.queryForObject(connection, sql, rowMapper, 1L)).isNull();
    }

    @Test
    @DisplayName("외부 커넥션의 단건 조회 결과가 여러 건이면 SQL과 정확한 건수를 담은 예외를 던진다")
    void query_for_object_with_multiple_rows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(connection, sql, rowMapper, 1L))
                .isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class)
                .hasMessageContaining("found 3")
                .hasMessageContaining(sql);
    }

    @Test
    @DisplayName("외부 커넥션에서 쿼리를 준비하다 실패하면 SQL과 원인을 보존한다")
    void prepare_statement_failure() throws SQLException {
        final var failure = new SQLException("Invalid SQL");
        when(connection.prepareStatement(sql)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(connection, sql, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
    }

    @Test
    @DisplayName("외부 커넥션의 쿼리 실패와 자원 해제 실패를 함께 보존한다")
    void resource_cleanup_failure() throws SQLException {
        final var failure = new SQLException("Execution failed");
        final var closeFailure = new SQLException("Close failed");
        when(statement.executeUpdate()).thenThrow(failure);
        doThrow(closeFailure).when(statement).close();
        final var updateSql = "update users set id = ?";

        assertThatThrownBy(() -> jdbcTemplate.update(connection, updateSql, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(updateSql)
                .hasCause(failure);
        assertThat(failure.getSuppressed()).containsExactly(closeFailure);
    }

    @Test
    @DisplayName("외부 커넥션의 setter에서 런타임 예외가 발생해도 Statement를 해제한다")
    void setter_runtime_failure() throws SQLException {
        final var failure = new IllegalArgumentException("Cannot bind parameter");
        doThrow(failure).when(statement).setLong(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, "update users set id = ?", setter))
                .isSameAs(failure);
        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection, never()).close();
    }

    @Test
    @DisplayName("외부 커넥션의 행 매핑이 실패하면 조회 자원을 해제하고 SQL과 원인을 보존한다")
    void row_mapping_sql_failure() throws SQLException {
        final var failure = new SQLException("Column unavailable");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(connection, sql, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
    }

    @Test
    @DisplayName("외부 커넥션의 행 매핑에서 발생한 런타임 예외는 그대로 전달한다")
    void row_mapping_runtime_failure() throws SQLException {
        final var failure = new IllegalArgumentException("Cannot map row");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(connection, sql, setter, rowMapper))
                .isSameAs(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
    }
}
