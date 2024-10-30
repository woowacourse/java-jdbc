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
        when(rs.next()).thenReturn(true, false);
        String sql = "select * from users where account = ?";

        User user = jdbcTemplate.queryForObject(sql, ps -> ps.setString(1, "hello"), getUserRowMapper());

        assertThat(user).isEqualTo(new User("hello", "1234", "my@email.com"));
    }

    @Test
    @DisplayName("값 하나를 조회할 수 없다면 예외가 발생한다.")
    void invalidQueryForObject() throws SQLException {
        when(rs.next()).thenReturn(false);
        String sql = "select * from users where account = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, ps -> ps.setString(1, "account"), getUserRowMapper()))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    @DisplayName("값 하나를 조회할 때, 조회 가능한 값이 여러개라면 예외가 발생한다.")
    void incorrectResultSize() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        String sql = "select * from users where account = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, ps -> ps.setString(1, "account"), getUserRowMapper()))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    @DisplayName("값 여러개를 조회할 수 있다.")
    void query() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        String sql = "select * from users";

        List<User> users = jdbcTemplate.query(sql, getUserRowMapper());

        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("값을 수정할 수 있다.")
    void update() throws SQLException {
        String sql = "update users set account=?, password=?, email=? where id=?";

        jdbcTemplate.update(conn, sql, ps -> {
            ps.setString(1, "account");
            ps.setString(2, "password");
            ps.setString(3, "email");
            ps.setLong(4, 1L);
        }, true);

        verify(pstmt).executeUpdate();
    }

    private RowMapper<User> getUserRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));
    }

    private record User(String account, String password, String email) {
    }
}
