package com.interface21.jdbc.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
}
