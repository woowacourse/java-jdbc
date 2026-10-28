package com.interface21.jdbc.core;

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
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final RowMapper<String> NAME_MAPPER = new RowMapper<>() {
        @Override
        public String mapRow(final ResultSet resultSet) throws SQLException {
            return resultSet.getString("name");
        }
    };

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    @DisplayName("전달한 인자를 SQL에 바인딩하고 변경한 행 수를 반환한다")
    void updateWithParameters() throws SQLException {
        // given
        final var sql = "update people set name = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(1);

        // when
        final var affectedRows = jdbcTemplate.update(sql, new Object[]{"gugu", 2L});

        // then
        assertThat(affectedRows).isEqualTo(1);
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, 2L);
    }

    @Test
    @DisplayName("변경 작업이 끝나면 Statement와 Connection을 해제한다")
    void closeResourcesAfterUpdate() throws SQLException {
        // given
        final var sql = "update people set name = 'gugu'";

        // when
        jdbcTemplate.update(sql, new Object[0]);

        // then
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 조건을 바인딩하고 모든 행을 호출자가 지정한 객체로 변환한다")
    void queryWithParameters() throws SQLException {
        // given
        final var sql = "select name from people where team = ?";
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("gugu", "other");

        // when
        final List<String> names = jdbcTemplate.query(sql, new Object[]{"team"}, NAME_MAPPER);

        // then
        assertThat(names).containsExactly("gugu", "other");
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, "team");
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
    void queryWhenEmpty() {
        // given
        final var sql = "select name from people";

        // when
        final var names = jdbcTemplate.query(sql, new Object[0], NAME_MAPPER);

        // then
        assertThat(names).isEmpty();
    }

    @Test
    @DisplayName("조회가 끝나면 ResultSet과 Statement와 Connection을 해제한다")
    void closeResourcesAfterQuery() throws SQLException {
        // given
        final var sql = "select name from people";

        // when
        jdbcTemplate.query(sql, new Object[0], NAME_MAPPER);

        // then
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("연결을 얻지 못하면 실패 원인을 호출자에게 전달한다")
    void failToGetConnection() throws SQLException {
        // given
        final var failure = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(failure);

        // when
        final var thrown = catchThrowable(() -> jdbcTemplate.update("update people set name = 'gugu'", new Object[0]));

        // then
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasCause(failure);
    }

    @Test
    @DisplayName("Statement 준비에 실패해도 획득한 연결을 해제한다")
    void failToPrepareStatement() throws SQLException {
        // given
        final var failure = new SQLException("prepare failed");
        when(connection.prepareStatement(anyString())).thenThrow(failure);

        // when
        final var thrown = catchThrowable(() -> jdbcTemplate.update("invalid sql", new Object[0]));

        // then
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasCause(failure);
        verify(connection).close();
    }

    @Test
    @DisplayName("변경 실행에 실패하면 원인을 전달하고 획득한 자원을 해제한다")
    void failToExecuteUpdate() throws SQLException {
        // given
        final var failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        // when
        final var thrown = catchThrowable(() -> jdbcTemplate.update("invalid sql", new Object[0]));

        // then
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 실행에 실패하면 원인을 전달하고 획득한 자원을 해제한다")
    void failToExecuteQuery() throws SQLException {
        // given
        final var failure = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(failure);

        // when
        final var thrown = catchThrowable(() -> jdbcTemplate.query("invalid sql", new Object[0], NAME_MAPPER));

        // then
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("결과 변환에 실패하면 원인을 전달하고 모든 조회 자원을 해제한다")
    void failToMapRow() throws SQLException {
        // given
        final var failure = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("name")).thenThrow(failure);

        // when
        final var thrown = catchThrowable(() -> jdbcTemplate.query("select name from people", new Object[0], NAME_MAPPER));

        // then
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
