package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class JdbcTemplateTest {

    private static final String SQL = "insert into users (account, password, email) values (?, ?, ?)";
    private static final String SELECT_SQL = "select account from users where id = ?";
    private static final RowMapper<String> ACCOUNT_ROW_MAPPER = (resultSet, rowNum) -> resultSet.getString("account");

    private DataSource dataSource;
    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void 파라미터가_주어지면_insert_시_1번_인덱스부터_순서대로_바인딩된다() throws SQLException {
        //given
        String sql = SQL;

        //when
        jdbcTemplate.insert(sql, "gugu", "password", "hkkang@woowahan.com");

        //then
        InOrder inOrder = inOrder(pstmt);
        inOrder.verify(pstmt).setObject(1, "gugu");
        inOrder.verify(pstmt).setObject(2, "password");
        inOrder.verify(pstmt).setObject(3, "hkkang@woowahan.com");
        inOrder.verify(pstmt).executeUpdate();
    }

    @Test
    void 파라미터가_없으면_insert_시_바인딩_없이_쿼리가_실행된다() throws SQLException {
        //given
        String sql = "insert into users (account, password, email) values ('gugu', 'password', 'hkkang@woowahan.com')";

        //when
        jdbcTemplate.insert(sql);

        //then
        verify(pstmt, never()).setObject(anyInt(), any());
        verify(pstmt).executeUpdate();
    }

    @Test
    void 올바른_SQL이_주어지면_insert_후_PreparedStatement와_Connection이_닫힌다() throws SQLException {
        //given
        String sql = SQL;

        //when
        jdbcTemplate.insert(sql, "gugu", "password", "hkkang@woowahan.com");

        //then
        InOrder inOrder = inOrder(pstmt, conn);
        inOrder.verify(pstmt).close();
        inOrder.verify(conn).close();
    }

    @Test
    void 쿼리_실행_중_예외가_발생해도_insert_후_자원은_닫힌다() throws SQLException {
        //given
        when(pstmt.executeUpdate()).thenThrow(new SQLException("insert 실패"));

        //when
        assertThatThrownBy(() -> jdbcTemplate.insert(SQL, "gugu", "password", "hkkang@woowahan.com"))
                .isInstanceOf(RuntimeException.class);

        //then
        verify(pstmt).close();
        verify(conn).close();
    }

    @Test
    void 커넥션_획득에_실패하면_insert_시_PreparedStatement를_닫지_않는다() throws SQLException {
        //given
        SQLException cause = new SQLException("커넥션 획득 실패");
        when(dataSource.getConnection()).thenThrow(cause);

        //when
        assertThatThrownBy(() -> jdbcTemplate.insert(SQL, "gugu", "password", "hkkang@woowahan.com"))
                .isInstanceOf(RuntimeException.class)
                .hasCause(cause);

        //then
        verify(pstmt, never()).close();
        verify(conn, never()).close();
    }

    @Test
    void 결과가_있으면_queryForObject_시_RowMapper로_변환한_객체를_반환한다() throws SQLException {
        //given
        when(rs.next()).thenReturn(true);
        when(rs.getString("account")).thenReturn("gugu");

        //when
        String account = jdbcTemplate.queryForObject(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L);

        //then
        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void 결과가_없으면_queryForObject_시_null을_반환한다() throws SQLException {
        //given
        when(rs.next()).thenReturn(false);

        //when
        String account = jdbcTemplate.queryForObject(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L);

        //then
        assertThat(account).isNull();
    }

    @Test
    void 파라미터가_주어지면_queryForObject_시_1번_인덱스부터_순서대로_바인딩된다() throws SQLException {
        //given
        String sql = "select account from users where id = ? and email = ?";

        //when
        jdbcTemplate.queryForObject(sql, ACCOUNT_ROW_MAPPER, 1L, "hkkang@woowahan.com");

        //then
        InOrder inOrder = inOrder(pstmt);
        inOrder.verify(pstmt).setObject(1, 1L);
        inOrder.verify(pstmt).setObject(2, "hkkang@woowahan.com");
        inOrder.verify(pstmt).executeQuery();
    }

    @Test
    void 조회가_끝나면_queryForObject_후_ResultSet_PreparedStatement_Connection_순서로_닫힌다() throws SQLException {
        //given
        when(rs.next()).thenReturn(true);

        //when
        jdbcTemplate.queryForObject(SELECT_SQL, ACCOUNT_ROW_MAPPER, 1L);

        //then
        InOrder inOrder = inOrder(rs, pstmt, conn);
        inOrder.verify(rs).close();
        inOrder.verify(pstmt).close();
        inOrder.verify(conn).close();
    }
}
