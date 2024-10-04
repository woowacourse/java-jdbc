package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<String> TEST_ROW_MAPPER = (rs, rowNum) -> "test";

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    @DisplayName("여러건 조회 쿼리를 실행한다.")
    void query() throws SQLException {
        String sql = "select * from test where arg1 = ? and arg2 = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        jdbcTemplate.query(sql, TEST_ROW_MAPPER, "arg1", "arg2");

        assertAll(
                () -> verify(preparedStatement).setObject(1, "arg1"),
                () -> verify(preparedStatement).setObject(2, "arg2")
        );
    }

    @Test
    @DisplayName("단일건 조회 쿼리를 실행한다.")
    void queryForObject() throws SQLException {
        String sql = "select * from test where arg1 = ? and arg2 = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        jdbcTemplate.queryForObject(sql, TEST_ROW_MAPPER, "arg1", "arg2");

        assertAll(
                () -> verify(preparedStatement).setObject(1, "arg1"),
                () -> verify(preparedStatement).setObject(2, "arg2")
        );
    }

    @Test
    @DisplayName("단일건 조회 쿼리문 실행 중 조회 결과가 없으면 예외가 발생한다.")
    void queryForObjectWhenNoResult() throws SQLException {
        String sql = "select * from test where arg1 = ? and arg2 = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_ROW_MAPPER, "arg1", "arg2"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("No result");
    }

    @Test
    @DisplayName("단일건 조회 쿼리문 수행 중 조획 결과가 2개 이상일 경우 예외가 발생한다.")
    void queryForObjectWhenMoreThanOne() throws SQLException {
        final String sql = "select * from test where arg1 = ? and arg2 = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_ROW_MAPPER, "arg1", "arg2"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Query returned more than one result.");
    }

    @Test
    @DisplayName("업데이트 쿼리를 실행한다.")
    void update() throws SQLException {
        final String sql = "update test set arg1 = ?, arg2 = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        jdbcTemplate.update(sql, "arg1", "arg2");

        assertAll(
                () -> verify(preparedStatement).setObject(1, "arg1"),
                () -> verify(preparedStatement).setObject(2, "arg2")
        );
    }

    @Test
    @DisplayName("결과 객체 변환이 올바르게 수행되는지 테스트한다.")
    void queryForObjectGetObject() throws SQLException {

        String sql = "SELECT arg1, arg2 FROM test WHERE id = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("arg1")).thenReturn(1);
        when(resultSet.getString("arg2")).thenReturn("arg2");

        TestObject result = jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new TestObject(
                        rs.getInt("arg1"),
                        rs.getString("arg2")
                ),
                1, "arg2"
        );

        assertAll(
                () -> assertThat(result.getArg1()).isEqualTo(1),
                () -> assertThat(result.getArg2()).isEqualTo("arg2")
        );
    }

    private static class TestObject {
        private final int arg1;
        private final String arg2;

        public TestObject(int arg1, String arg2) {
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        public int getArg1() {
            return arg1;
        }

        public String getArg2() {
            return arg2;
        }
    }
}
