package com.interface21.jdbc.core;

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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    @DisplayName("변경 쿼리는 인자를 순서대로 바인딩하고 실행한 뒤 자원을 닫는다")
    void commandBindsParametersAndExecutesUpdate() throws SQLException {
        final var sql = "update users set account = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        jdbcTemplate.command(sql, "gugu", 1L);

        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, 1L);
        verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("목록 조회는 인자를 바인딩하고 모든 행을 변환한 뒤 자원을 닫는다")
    void queryMapsEveryRowAndBindsParameters() throws SQLException {
        final var sql = "select account from users where id > ?";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "isaac");

        final var results = jdbcTemplate.query(sql, rs -> rs.getString("account"), 0L);

        assertThat(results).containsExactly("gugu", "isaac");
        verify(statement).setObject(1, 0L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
    void queryReturnsEmptyListWhenNoRowsExist() throws SQLException {
        final var sql = "select account from users";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(false);

        assertThat(jdbcTemplate.query(sql, rs -> rs.getString("account"))).isEmpty();
    }

    @Test
    @DisplayName("단건 조회 결과가 한 행이면 변환된 객체를 반환한다")
    void queryForObjectReturnsSingleMappedObject() throws SQLException {
        final var sql = "select account from users where id = ?";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var result = jdbcTemplate.queryForObject(sql, rs -> rs.getString("account"), 1L);

        assertThat(result).isEqualTo("gugu");
        verify(statement).setObject(1, 1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 null을 반환한다")
    void queryForObjectReturnsNullWhenNoRowsExist() throws SQLException {
        final var sql = "select account from users where id = ?";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(false);

        final var result = jdbcTemplate.queryForObject(sql, rs -> rs.getString("account"), 1L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("단건 조회 결과가 여러 행이면 예외가 발생한다")
    void queryForObjectRejectsMultipleRows() throws SQLException {
        final var sql = "select account from users";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "isaac");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rs -> rs.getString("account")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("변경 쿼리 실행이 실패하면 원인 예외를 보존하고 자원을 닫는다")
    void commandPreservesSqlExceptionAndClosesResources() throws SQLException {
        final var sql = "insert into users (account) values (?)";
        final var failure = new SQLException("execution failed");
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.command(sql, "gugu"))
                .isInstanceOf(RuntimeException.class)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("행 변환이 실패하면 원인 예외를 보존하고 조회 자원을 모두 닫는다")
    void queryClosesResourcesWhenMappingFails() throws SQLException {
        final var sql = "select account from users";
        final var failure = new SQLException("mapping failed");
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(sql, rs -> {
            throw failure;
        })).isInstanceOf(RuntimeException.class).hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과를 닫다가 실패해도 나머지 자원을 닫고 결과를 반환한다")
    void queryStillClosesOtherResourcesWhenResultSetCloseFails() throws SQLException {
        final var sql = "select account from users";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(false);
        doThrow(new SQLException("close failed")).when(resultSet).close();

        assertThat(jdbcTemplate.query(sql, rs -> rs.getString("account"))).isEmpty();

        verify(statement).close();
        verify(connection).close();
    }

    private void prepareQuery(final String sql) throws SQLException {
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }
}
