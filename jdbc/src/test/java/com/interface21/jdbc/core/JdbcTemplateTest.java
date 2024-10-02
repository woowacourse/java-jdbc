package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
    void set() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("여러건 조회 쿼리 수행한다.")
    void query() throws SQLException {
        String sql = "select * from test where arg1 = ? and arg2 = ?";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        jdbcTemplate.query(sql, TEST_ROW_MAPPER, "arg1", "arg2");

        assertAll(
                () -> verify(preparedStatement, times(2)).setObject(anyInt(), any()),
                () -> verify(resultSet, times(3)).next()
        );
    }

    @Test
    @DisplayName("단일건 조회 쿼리를 실행한다.")
    void queryForObject() throws SQLException {
        String sql = "select * from test where arg1 = ? and arg2 = ?";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getRow()).thenReturn(1);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        jdbcTemplate.queryForObject(sql, TEST_ROW_MAPPER, "arg1", "arg2");

        assertAll(
                () -> verify(preparedStatement, times(2)).setObject(anyInt(), any()),
                () -> verify(resultSet, times(2)).next()
        );
    }

    @Test
    @DisplayName("단일건 조회 쿼리문 실행 중 조회 결과가 없으면 예외가 발생한다.")
    void queryForObject_noResult() throws SQLException {
        String sql = "select * from test where arg1 = ? and arg2 = ?";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_ROW_MAPPER, "arg1", "arg2"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("No result");
    }

    @Test
    @DisplayName("단일건 조회 쿼리문 수행 중 조획 결과가 2개 이상일 경우 예외가 발생한다.")
    void queryForObject_moreThanOne() throws SQLException {
        final String sql = "select * from test where arg1 = ? and arg2 = ?";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_ROW_MAPPER, "arg1", "arg2"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Query returned more than one result.");
    }

    @Test
    @DisplayName("업데이트 쿼리를 실행한다..")
    void update() throws SQLException {
        final String sql = "update test set arg1 = ?, arg2 = ?";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(preparedStatement);

        jdbcTemplate.update(sql, "arg1", "arg2");

        verify(preparedStatement, times(2)).setObject(anyInt(), any(String.class));
    }
}
