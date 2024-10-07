package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        // Given
        final String sql = "UPDATE users SET name = ? WHERE id = ?";
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // When
        final int result = jdbcTemplate.update(sql, "Redddy", 1);

        // Then
        assertAll(
                () -> assertThat(result).isEqualTo(1),
                () -> verify(preparedStatement).setObject(1, "Redddy"),
                () -> verify(preparedStatement).setObject(2, 1),
                () -> verify(preparedStatement).executeUpdate(),
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close()
        );
    }

    @Test
    void queryForObject() throws SQLException {
        // Given
        final String sql = "SELECT id, name FROM users WHERE id = ?";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Redddy");
        final User expected = new User(1, "Redddy");

        final RowMapper<User> rowMapper = (rs, rowNum) -> new User(rs.getInt("id"), rs.getString("name"));

        // When
        final User user = jdbcTemplate.queryForObject(sql, rowMapper, 1);

        // Then
        assertAll(
                () -> assertThat(user).isEqualTo(expected),
                () -> verify(preparedStatement).setObject(1, 1),
                () -> verify(preparedStatement).executeQuery(),
                () -> verify(preparedStatement).close(),
                () -> verify(connection).close()
        );
    }

    @Test
    void queryForList() throws SQLException {
        // Given
        final String sql = "SELECT id, name FROM users";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("Redddy", "ChocoChip");
        final List<User> expected = List.of(new User(1, "Redddy"), new User(2, "ChocoChip"));

        final RowMapper<User> rowMapper = (rs, rowNum) -> new User(rs.getInt("id"), rs.getString("name"));

        // When
        final List<User> users = jdbcTemplate.queryForList(sql, rowMapper);

        // Then
        assertAll(
                () -> assertThat(users).isEqualTo(expected),
                () -> verify(preparedStatement).executeQuery(),
                () -> verify(preparedStatement).close(),
                () -> verify(connection).close()
        );
    }

    private record User(int id, String name) {
    }
}
