package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;
    private ParameterMetaData parameterMetaData;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        preparedStatement = mock(PreparedStatement.class);
        connection = mock(Connection.class);
        parameterMetaData = mock(ParameterMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(preparedStatement.getParameterMetaData()).thenReturn(parameterMetaData);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("sql문의 파라미터 개수와 매개변수로 전달된 파라미터 개수가 일치하지 않을 시 예외를 발생시킨다")
    @Test
    void validateParameterCount() throws SQLException {
        String sql = "insert into samples (name) values (?)";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(parameterMetaData.getParameterCount()).thenReturn(1);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "test", "test"))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("단건 조회의 결과로 2개 이상의 결과가 나올 시 예외를 발생시킨다")
    @Test
    void queryForObjectNotOneResult() throws SQLException {
        String sql = "select id, name from samples where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(parameterMetaData.getParameterCount()).thenReturn(1);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("lemone1", "lemone2");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        RowMapper<Sample> rowMapper = rs -> new Sample(rs.getLong("id"), rs.getString("name"));

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("단건 조회의 결과가 없을 시 예외를 발생시킨다")
    @Test
    void queryForObjectNotExistResult() throws SQLException {
        String sql = "select id, name from samples where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(parameterMetaData.getParameterCount()).thenReturn(1);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(false);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        RowMapper<Sample> rowMapper = rs -> new Sample(rs.getLong("id"), rs.getString("name"));

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1))
                .isInstanceOf(DataAccessException.class);
    }

    record Sample(long id, String name) {
    }
}
