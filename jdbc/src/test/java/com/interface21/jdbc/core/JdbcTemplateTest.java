package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource = mock(DataSource.class);
    private Connection connection = mock(Connection.class);
    private PreparedStatement pstmt = mock(PreparedStatement.class);
    private ResultSet resultSet = mock(ResultSet.class);

    private static RowMapper<String> mapper = rs -> "test";
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("queryForObject() 호출시, 조회 되는 값이 없으면 예외를 발생시킨다.")
    @Test
    void queryForObject_ifEmptyResult_throwException() throws SQLException {

        final var sql = "select * from users";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, mapper))
                .isExactlyInstanceOf(EmptyResultDataAccessException.class);
    }

    @DisplayName("queryForObject() 호출시, 조회 되는 값이 1개 이상이면 예외를 발생시킨다.")
    @Test
    void queryForObject_ifMoreThanOneResult_throwException() throws SQLException {
        final var sql = "select * from users";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(pstmt);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(pstmt.executeQuery()).thenReturn(resultSet);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, mapper))
                .isExactlyInstanceOf(IncorrectResultSizeDataAccessException.class);
    }
}
