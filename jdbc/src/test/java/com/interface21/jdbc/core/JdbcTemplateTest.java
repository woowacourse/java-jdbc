package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.jdbc.datasource.DataSourceUtils;


class JdbcTemplateTest {

    private static final RowMapper<TestClass> TEST_CLASS_ROW_MAPPER = (rs, rowNum) -> new TestClass(
            rs.getString("column1"),
            rs.getString("column2"));

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final Connection connection = mock(Connection.class);
        final DataSource dataSource = mock(DataSource.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("read 메소드로 쿼리 실행")
    void read() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("column1")).thenReturn("value1");
        when(resultSet.getString("column2")).thenReturn("value2");

        TestClass expectedResult = new TestClass("value1", "value2");

        Optional<TestClass> result =
                jdbcTemplate.read("select * from test where id = ?", TEST_CLASS_ROW_MAPPER, "value1", "value2");

        assertThat(result).contains(expectedResult);
    }

    @Test
    @DisplayName("write 메소드로 쿼리 실행")
    void write() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        jdbcTemplate.write("insert into test (column1, column2) values (?, ?)", "value1", "value2");

        verify(preparedStatement).setObject(1, "value1");
        verify(preparedStatement).setObject(2, "value2");
        verify(preparedStatement).executeUpdate();
    }

    private record TestClass(String str1, String str2) {
    }
}
