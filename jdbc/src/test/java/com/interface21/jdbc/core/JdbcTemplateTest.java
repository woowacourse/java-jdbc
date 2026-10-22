package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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

    @DisplayName("update 실행 중 예외가 발생해도 자원을 반납하고 원인 예외를 보존한다")
    @Test
    void updateFail() throws SQLException {
        SQLException cause = new SQLException("update 실패");
        when(pstmt.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("update users set account = ?", "gugu"))
                .hasCause(cause);

        verify(pstmt).close();
        verify(conn).close();
    }

    @DisplayName("조회 결과를 RowMapper로 변환하고 자원을 반납한다")
    @Test
    void query() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("account")).thenReturn("gugu", "tas");

        var accounts = jdbcTemplate.query("select account from users", rs -> rs.getString("account"));

        assertThat(accounts).containsExactly("gugu", "tas");
        verify(rs).close();
        verify(pstmt).close();
        verify(conn).close();
    }

    @DisplayName("조회 콜백에서 예외가 발생해도 자원을 반납하고 원인 예외를 보존한다")
    @Test
    void queryFailInRowMapper() throws SQLException {
        SQLException cause = new SQLException("매핑 실패");
        when(rs.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rs -> {
            throw cause;
        })).hasCause(cause);

        verify(rs).close();
        verify(pstmt).close();
        verify(conn).close();
    }

    @DisplayName("단건 조회 결과가 없으면 null을 반환한다")
    @Test
    void queryForObjectEmpty() throws SQLException {
        when(rs.next()).thenReturn(false);

        var account = jdbcTemplate.queryForObject("select account from users where id = ?",
                rs -> rs.getString("account"), 1L);

        assertThat(account).isNull();
    }
}
