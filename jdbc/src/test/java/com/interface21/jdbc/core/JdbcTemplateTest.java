package com.interface21.jdbc.core;

import com.interface21.jdbc.exception.EmptyResultDataAccessException;
import com.interface21.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource = mock(DataSource.class);
    private Connection conn = mock(Connection.class);
    private PreparedStatement pstmt = mock(PreparedStatement.class);
    private ResultSet rs = mock(ResultSet.class);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("값 하나를 조회할 수 있다.")
    void queryForObject() throws SQLException {
        when(rs.getString("account")).thenReturn("hello");
        when(rs.getString("password")).thenReturn("1234");
        when(rs.getString("email")).thenReturn("my@email.com");
        when(rs.next()).thenReturn(true);
        String sql = "select * from users where account = ?";

        User user = jdbcTemplate.queryForObject(sql, getUserRowMapper(), "hello");

        assertThat(user).isEqualTo(new User("hello", "1234", "my@email.com"));
    }

    @Test
    @DisplayName("값 하나를 조회할 수 없다면 예외가 발생한다.")
    void invalidQueryForObject() throws SQLException {
        when(rs.next()).thenReturn(false);
        String sql = "select * from users where account = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, getUserRowMapper(), "hello"))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    @DisplayName("값 하나를 조회할 때, 조회 가능한 값이 여러개라면 예외가 발생한다.")
    void incorrectResultSize() throws SQLException {
        when(rs.next()).thenReturn(true, true);
        String sql = "select * from users where account = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, getUserRowMapper(), "hello"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    @DisplayName("값 여러개를 조회할 수 있다.")
    void query() throws SQLException {
        when(rs.getString("account")).thenReturn("hello");
        when(rs.getString("password")).thenReturn("1234");
        when(rs.getString("email")).thenReturn("my@email.com");
        when(rs.next()).thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        String sql = "select * from users";

        List<User> users = jdbcTemplate.query(sql, getUserRowMapper());

        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("값을 수정할 수 있다.")
    void update() throws SQLException {
        String sql = "update users set account=?, password=?, email=? where id=?";

        jdbcTemplate.update(sql, "account", "password", "email", 1L);

        verify(pstmt).executeUpdate();
    }

    private RowMapper<User> getUserRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
    }

    private static class User {

        private final String account;
        private final String password;
        private final String email;

        public User(String account, String password, String email) {
            this.account = account;
            this.password = password;
            this.email = email;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(account, user.account) && Objects.equals(password, user.password) && Objects.equals(email, user.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(account, password, email);
        }

        @Override
        public String toString() {
            return "User{" +
                    "account='" + account + '\'' +
                    ", password='" + password + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }
}
