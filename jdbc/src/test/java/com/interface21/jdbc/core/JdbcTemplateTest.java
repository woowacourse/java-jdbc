package com.interface21.jdbc.core;

import static org.mockito.Mockito.*;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.mapper.Mapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource source;
    private ResultSet resultSet;

    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void beforeAll() {
        mockStatic(Mapper.class);
    }

    @BeforeEach
    void setUp() throws SQLException {
        source = mock(DataSource.class);
        resultSet = mock(ResultSet.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(source.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(source);
    }


    @DisplayName("단건 조회시 데이터가 없다면 null을 반환한다.")
    @Test
    void queryNull() {
        Assertions.assertThat(jdbcTemplate.query(String.class, "select * from user")).isNull();
    }

    @DisplayName("단건 조회시 데이터가 두 개 이상이라면 예외가 발생한다.")
    @Test
    void queryDuplicatedData() throws SQLException {
        String sql = "select id from users";
        when(Mapper.doQueryMapping(String.class, sql, resultSet)).thenReturn(List.of("one", "two"));

        Assertions.assertThatThrownBy(() -> jdbcTemplate.query(String.class, sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("한 건 이상의 데이터가 조회되었습니다.");
    }

    @DisplayName("리스트로 조회시 데이터가 없다면 빈리스트가 반환된다.")
    @Test
    void queryForAllEmptyList() throws SQLException {
        String sql = "select id from users";

        when(Mapper.doQueryMapping(String.class, sql, resultSet)).thenReturn(List.of());

        Assertions.assertThat(jdbcTemplate.queryForAll(String.class, sql)).isEmpty();
    }


}
