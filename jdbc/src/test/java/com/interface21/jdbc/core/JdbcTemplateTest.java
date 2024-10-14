package com.interface21.jdbc.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.mockito.Mockito;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("사용자 데이터를 insert 할 때, 데이터베이스에 성공적으로 저장되어야 한다")
    @Test
    void insert() throws SQLException {
        // given
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        String account = "chocochip";
        String password = "password123";
        String email = "chocochip@email.com";

        // when
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        int rowsAffected = jdbcTemplate.update(sql, account, password, email);

        // then
        assertEquals(1, rowsAffected);
        verify(preparedStatement).setObject(1, account);
        verify(preparedStatement).setObject(2, password);
        verify(preparedStatement).setObject(3, email);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
    }

    @Test
    @DisplayName("특정 계정으로 사용자를 조회할 때, 올바른 사용자 정보를 반환해야 한다")
    void testQueryForObject() throws SQLException {
        // given
        String sql = "SELECT id, account, password, email FROM users WHERE account = ?";
        String account = "chocochip";
        String password = "password123";
        String email = "chocochip@email.com";

        // when
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn(account);
        when(resultSet.getString("password")).thenReturn(password);
        when(resultSet.getString("email")).thenReturn(email);

        User user = jdbcTemplate.queryForObject(sql, rs -> {
            try {
                return new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, account);

        // then
        assertEquals(account, user.getAccount());
        assertEquals(password, user.getPassword());
        assertEquals(email, user.getEmail());
        verify(preparedStatement).setObject(1, account);
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
        verify(resultSet).close();
        verify(preparedStatement).close();
    }

    static class User {

        private final Long id;
        private final String account;
        private final String password;
        private final String email;

        public User(Long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public String getAccount() {
            return account;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }
    }
}
