package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void query() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("test");

        String sql = "SELECT name FROM users";
        List<String> results = jdbcTemplate.query(sql, rs -> rs.getString("name"));

        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo("test");
    }

    @Test
    void queryForObject() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("test");

        String sql = "SELECT name FROM users WHERE id = 1";
        String result = jdbcTemplate.queryForObject(sql, rs -> rs.getString("name"));

        assertThat(result).isEqualTo("test");
    }

    @Test
    void update() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        String sql = "UPDATE users SET name = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, "newName", 1);

        assertThat(rowsAffected).isEqualTo(1);
    }
}
