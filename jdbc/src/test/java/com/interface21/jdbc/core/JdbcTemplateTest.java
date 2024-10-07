package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void 하나의_값을_조회한다() throws SQLException {
        final String sql = "SELECT id, name, email FROM users WHERE id = ?";

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("name")).thenReturn("abc");
        when(resultSet.getString("email")).thenReturn("abc@abc.com");

        final TestUser testUser = jdbcTemplate.queryForObject(sql, new TestUserRowMapper(), 1L);

        Assertions.assertAll(
                () -> assertThat(testUser).isNotNull(),
                () -> assertThat(testUser.getId()).isEqualTo(1L),
                () -> assertThat(testUser.getName()).isEqualTo("abc"),
                () -> assertThat(testUser.getEmail()).isEqualTo("abc@abc.com")
        );
    }

    @Test
    void 모든_값을_조회한다() throws SQLException {
        final String sql = "SELECT id, name, email FROM users";

        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("name")).thenReturn("abc", "def");
        when(resultSet.getString("email")).thenReturn("abc@abc.com", "def@def.com");

        final List<TestUser> testUsers = jdbcTemplate.query(sql, new TestUserRowMapper());

        assertThat(testUsers).hasSize(2);

        final TestUser firstUser = testUsers.getFirst();
        final TestUser secondUser = testUsers.get(1);

        Assertions.assertAll(
                () -> assertThat(firstUser.getId()).isEqualTo(1L),
                () -> assertThat(firstUser.getName()).isEqualTo("abc"),
                () -> assertThat(firstUser.getEmail()).isEqualTo("abc@abc.com"),
                () -> assertThat(secondUser.getId()).isEqualTo(2L),
                () -> assertThat(secondUser.getName()).isEqualTo("def"),
                () -> assertThat(secondUser.getEmail()).isEqualTo("def@def.com")
        );
    }

    @Test
    void 값을_수정한다() throws SQLException {
        final String sql = "UPDATE users SET account = ?, email = ? WHERE id = ?";
        when(preparedStatement.executeUpdate()).thenReturn(1);

        jdbcTemplate.update(sql, "abc", "abc@abc.com", 1L);

        verify(preparedStatement).setObject(1, "abc");
        verify(preparedStatement).setObject(2, "abc@abc.com");
        verify(preparedStatement).executeUpdate();
    }
}

class TestUserRowMapper implements RowMapper<TestUser> {
    @Override
    public TestUser mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        return new TestUser(id, name, email);
    }
}
