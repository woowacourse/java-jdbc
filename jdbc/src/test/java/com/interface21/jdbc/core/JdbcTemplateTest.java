package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        jdbcTemplate.update("update users set account = ? where id = ?", "gugu", 1L);

        verify(pstmt).setObject(1, "gugu");
        verify(pstmt).setObject(2, 1L);
        verify(pstmt).executeUpdate();
        verify(pstmt).close();
        verify(conn).close();
    }

    @Test
    void query() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("account")).thenReturn("gugu", "brown");

        List<String> accounts = jdbcTemplate.query("select account from users", rs -> rs.getString("account"));

        assertThat(accounts).containsExactly("gugu", "brown");
    }

    @Test
    void queryForObject_결과가_없으면_예외() throws SQLException {
        when(rs.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users where id = ?",
                rs -> rs.getString("account"), 1L))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void SQLException이_발생해도_커넥션을_닫는다() throws SQLException {
        when(pstmt.executeUpdate()).thenThrow(new SQLException());

        assertThatThrownBy(() -> jdbcTemplate.update("insert into users (account) values (?)", "gugu"))
                .isInstanceOf(DataAccessException.class);
        verify(conn).close();
    }
}
