package com.interface21.jdbc.core;

import com.interface21.jdbc.result.RowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private final RowMapper<TestObject> rowMapper =
            resultSet -> new TestObject(resultSet.getLong("id"), resultSet.getString("content"));

    @BeforeEach
    void setup() throws SQLException {
        final DataSource dataSource = getDataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("쿼리문을 실행해 객체를 생성한다.")
    void query_for_object() {
        final var readOne = "select * from test where id = ?";
        final var result = jdbcTemplate.queryForObject(readOne, rowMapper, 1L);

        assertThat(result).isEqualTo(new TestObject(1L, "sample"));
    }

    @Test
    @DisplayName("쿼리문을 실행해 리스트를 생성한다.")
    void query_for_list() {
        final var readOne = "select * from";
        final var result = jdbcTemplate.queryForList(readOne, rowMapper);

        assertThat(result).containsAnyOf(
                new TestObject(1L, "sample"),
                new TestObject(2L, "sample2")
        );
    }

    @Test
    @DisplayName("쿼리문을 실행해 스트림을 생성한다.")
    void query_for_stream() {
        final var readOne = "select * from";
        final var result = jdbcTemplate.queryForStream(readOne, rowMapper);

        assertThat(result).containsAnyOf(
                new TestObject(1L, "sample"),
                new TestObject(2L, "sample2")
        );
    }


    private DataSource getDataSource() throws SQLException {
        final Connection connection = Mockito.mock(Connection.class);
        final DataSource dataSource = Mockito.mock(DataSource.class);
        final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenReturn(preparedStatement);

        final ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);  // 2개의 행 반환
        Mockito.when(resultSet.getString("content"))
                .thenReturn("sample")
                .thenReturn("sample2");
        Mockito.when(resultSet.getLong("id"))
                .thenReturn(1L)
                .thenReturn(2L);

        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);

        return dataSource;
    }

}
