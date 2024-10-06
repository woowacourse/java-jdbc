package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.interface21.jdbc.support.TestUser;

class JdbcTemplateTest {

    @Mock
    DataSource dataSource;

    @Mock
    Connection connection;

    @Mock
    PreparedStatement preparedStatement;

    @Mock
    ResultSet resultSet;

    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @DisplayName("단일 객체를 반환한다. - 단일 파라미터")
    @Test
    void queryForObject() throws SQLException {
        // given
        String sql = "SELECT id, name FROM users WHERE id = ?";

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("Tebah");

        // when
        TestUser user = jdbcTemplate.queryForObject(
            sql,
            (resultSet, rowNumber) ->
                new TestUser(
                    resultSet.getLong("id"),
                    resultSet.getString("name")),
            1L);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Tebah");

        verify(preparedStatement).setObject(1, 1L);
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
    }

    @DisplayName("단일 객체를 반환한다 - 멀티 파라미터")
    @Test
    void queryForObject_multiParams() throws SQLException {
        // given
        String sql = "SELECT id, name FROM users WHERE id = ? AND name = ?";

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("Tebah");

        // when
        TestUser user = jdbcTemplate.queryForObject(
            sql,
            (resultSet, rowNumber) ->
                new TestUser(
                    resultSet.getLong("id"),
                    resultSet.getString("name")),
            1L, "Tebah");

        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Tebah");

        verify(preparedStatement).setObject(1, 1L);
        verify(preparedStatement).setObject(2, "Tebah");
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
    }

    @DisplayName("INSERT, DELETE, UPDATE 작업을 처리한다.")
    @Test
    void update() throws SQLException {
        // given
        String sql = "INSERT INTO users (id, name) VALUES (?, ?)";

        when(preparedStatement.executeUpdate()).thenReturn(1);

        // when
        int affectedRows = jdbcTemplate.update(sql, 1L, "Tebah");

        // then
        assertThat(affectedRows).isEqualTo(1);

        verify(preparedStatement).setObject(1, 1L);
        verify(preparedStatement).setObject(2, "Tebah");
        verify(preparedStatement).executeUpdate();
    }
}
