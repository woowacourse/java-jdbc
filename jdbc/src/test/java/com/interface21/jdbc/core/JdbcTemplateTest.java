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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {
    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(pstmt);
        given(pstmt.executeQuery()).willReturn(rs);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update는_인자를_순서대로_바인딩하고_영향받은_행_수를_반환한다() throws SQLException {
        given(pstmt.executeUpdate()).willReturn(1);

        final int result = jdbcTemplate.update(connection, "insert into users (account, id) values (?, ?)", "gugu", 1L);

        assertThat(result).isEqualTo(1);
        final InOrder order = inOrder(pstmt);
        order.verify(pstmt).setObject(1, "gugu");
        order.verify(pstmt).setObject(2, 1L);
        order.verify(pstmt).executeUpdate();
    }

    @Test
    void 정상_종료_시_자신이_연_자원만_닫고_받은_커넥션은_닫지_않는다() throws SQLException {
        given(rs.next()).willReturn(false);

        jdbcTemplate.query(connection, "select * from users", (r, n) -> r.getString("account"));

        verify(rs).close();
        verify(pstmt).close();
        verify(connection, never()).close();
    }

    @Test
    void 예외가_발생해도_자신이_연_자원은_닫고_DataAccessException으로_변환한다() throws SQLException {
        given(pstmt.executeUpdate()).willThrow(new SQLException("boom"));

        assertThatThrownBy(() -> jdbcTemplate.update(connection, "insert into users values (?)", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        verify(pstmt).close();
        verify(connection, never()).close();
    }

    @Test
    void query는_모든_행을_RowMapper로_매핑한다() throws SQLException {
        given(rs.next()).willReturn(true, true, false);
        given(rs.getString("account")).willReturn("gugu", "hkkang");

        final List<String> result = jdbcTemplate.query(connection, "select account from users", (r, n) -> r.getString("account"));

        assertThat(result).containsExactly("gugu", "hkkang");
    }

    @Test
    void queryForObject는_결과가_없으면_빈_Optional을_반환한다() throws SQLException {
        given(rs.next()).willReturn(false);

        final Optional<String> result = jdbcTemplate.queryForObject(
                connection,
                "select account from users where id = ?", (r, n) -> r.getString("account"), 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void queryForObject는_결과가_2건_이상이면_예외를_던진다() throws SQLException {
        given(rs.next()).willReturn(true, true, false);
        given(rs.getString("account")).willReturn("gugu", "gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                connection,
                "select account from users where account = ?", (r, n) -> r.getString("account"), "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("2건");
    }
}
