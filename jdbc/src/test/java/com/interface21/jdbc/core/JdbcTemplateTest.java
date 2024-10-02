package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("queryForObject() 조회 결과가 존재하지 않으면 예외를 반환한다.")
    @Test
    void throwsWhenNoResult() throws SQLException {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";

        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(rs.next()).thenReturn(false);
        when(pstmt.executeQuery()).thenReturn(rs);

        RowMapper<User> rowMapper = mock(RowMapper.class);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("queryForObject() 조회 결과가 여러개면 예외를 반환한다.")
    @Test
    void throwsWhenMultipleResults() throws SQLException {
        String sql = "SELECT id, account, password, email FROM users WHERE id = ?";

        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(rs.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(pstmt.executeQuery()).thenReturn(rs);

        RowMapper<User> rowMapper = mock(RowMapper.class);
        when(rowMapper.mapRow(rs)).thenReturn(new User("jazz", "1130", "jazz@woowahan.com"));

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class);
    }


    static class User {
        private Long id;
        private String account;
        private String password;
        private String email;

        public User(String account, String password, String email) {
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public String getAccount() {
            return account;
        }

        public long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }
}
