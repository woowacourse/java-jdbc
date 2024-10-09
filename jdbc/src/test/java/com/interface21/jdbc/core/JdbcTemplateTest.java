package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {
    private final RowMapper<Object> testRowMapper = (resultSet, rowNum) -> new Object();
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    @DisplayName("update 호출 시 자원 반환 여부 확인")
    void update() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.update("query");

        assertAll(
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close()
        );
    }

    @Test
    @DisplayName("queryForObject 호출 시 자원 반환 여부 확인")
    void queryForObject() throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        when(resultSet.isLast()).thenReturn(true);
        jdbcTemplate.queryForObject("query", testRowMapper);

        assertAll(
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(resultSet).close()
        );
    }

    @Test
    @DisplayName("query 호출 시 자원 반환 여부 확인")
    void query() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.query("query", testRowMapper);

        assertAll(
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(resultSet).close()
        );
    }
}
