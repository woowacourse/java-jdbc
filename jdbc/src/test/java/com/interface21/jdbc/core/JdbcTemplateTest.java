package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
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
        when(pstmt.executeUpdate()).thenReturn(1);

        int updatedRows = jdbcTemplate.update("update users set account = ? where id = ?", "gugu", 1L);

        verify(pstmt).setObject(1, "gugu");
        verify(pstmt).setObject(2, 1L);
        verify(pstmt).executeUpdate();
        assertThat(updatedRows).isEqualTo(1);
        verify(pstmt).close();
        verify(conn).close();
    }

    @Test
    void update_PreparedStatementSetter() throws SQLException {
        when(pstmt.executeUpdate()).thenReturn(1);

        int updatedRows = jdbcTemplate.update("update users set account = ? where id = ?", ps -> {
            ps.setString(1, "gugu");
            ps.setLong(2, 1L);
        });

        verify(pstmt).setString(1, "gugu");
        verify(pstmt).setLong(2, 1L);
        assertThat(updatedRows).isEqualTo(1);
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
    void update_값을_바인딩한_뒤_실행한다() throws SQLException {
        jdbcTemplate.update("insert into users (account, password) values (?, ?)", "gugu", "password");

        InOrder inOrder = inOrder(pstmt);
        inOrder.verify(pstmt).setObject(1, "gugu");
        inOrder.verify(pstmt).setObject(2, "password");
        inOrder.verify(pstmt).executeUpdate();
    }

    @Test
    void update_PreparedStatementSetter가_null이면_바인딩_없이_실행한다() throws SQLException {
        jdbcTemplate.update("delete from users", (PreparedStatementSetter) null);

        verify(pstmt).executeUpdate();
    }

    @Test
    void update_가변인자_배열이_null이면_바인딩_없이_실행한다() throws SQLException {
        jdbcTemplate.update("delete from users", (Object[]) null);

        verify(pstmt).executeUpdate();
    }

    @Test
    void update_바인딩할_값으로_null을_넘기면_null로_바인딩한다() throws SQLException {
        jdbcTemplate.update("update users set email = ? where id = ?", null, 1L);

        verify(pstmt).setObject(1, null);
        verify(pstmt).setObject(2, 1L);
    }

    @Test
    void query_PreparedStatementSetter() throws SQLException {
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("account")).thenReturn("gugu");

        List<String> accounts = jdbcTemplate.query("select account from users where id = ?",
                rs -> rs.getString("account"), ps -> ps.setLong(1, 1L));

        verify(pstmt).setLong(1, 1L);
        assertThat(accounts).containsExactly("gugu");
    }

    @Test
    void query_ResultSet과_자원을_닫는다() throws SQLException {
        when(rs.next()).thenReturn(false);

        jdbcTemplate.query("select account from users", rs -> rs.getString("account"));

        verify(rs).close();
        verify(pstmt).close();
        verify(conn).close();
    }

    @Test
    void queryForObject_결과가_1개면_반환한다() throws SQLException {
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("account")).thenReturn("gugu");

        String account = jdbcTemplate.queryForObject("select account from users where id = ?",
                rs -> rs.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
        verify(pstmt).setObject(1, 1L);
    }

    @Test
    void queryForObject_결과가_2개_이상이면_예외() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("account")).thenReturn("gugu", "brown");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users",
                rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void SQLException이_발생하면_DataAccessException으로_바꾼다() throws SQLException {
        when(pstmt.executeUpdate()).thenThrow(new SQLException());

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void SQLException이_발생해도_커넥션을_닫는다() throws SQLException {
        when(pstmt.executeUpdate()).thenThrow(new SQLException());

        assertThatThrownBy(() -> jdbcTemplate.update("insert into users (account) values (?)", "gugu"))
                .isInstanceOf(DataAccessException.class);
        verify(pstmt).close();
        verify(conn).close();
    }
}
