package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        jdbcTemplate.update("INSERT INTO users (account, password, email) VALUES (?, ?, ?)", "gugu", "password", "gugu@email.com");

        verify(preparedStatement).executeUpdate();
    }

    @Test
    void queryForObject() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final User user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE account = ?", (rs) -> new User(rs.getString("account")), "gugu");

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void query() throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "jason");

        final List<User> users = jdbcTemplate.query("SELECT * FROM users", (rs) -> new User(rs.getString("account")));

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getAccount()).isEqualTo("gugu");
        assertThat(users.get(1).getAccount()).isEqualTo("jason");
    }

    private static class User {
        private final String account;

        public User(String account) {
            this.account = account;
        }

        public String getAccount() {
            return account;
        }
    }
}