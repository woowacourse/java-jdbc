package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void update는_인자를_순서대로_채우고_실행한_뒤_자원을_닫는다() throws SQLException {
        final var sql = "insert into users (account, password) values (?, ?)";

        jdbcTemplate.update(sql, "gugu", "password");

        verify(connection).prepareStatement(sql);
        verify(pstmt).setObject(1, "gugu");
        verify(pstmt).setObject(2, "password");
        verify(pstmt).executeUpdate();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void update는_PreparedStatementSetter로_직접_값을_채울_수_있다() throws SQLException {
        final var sql = "update users set password = ? where id = ?";

        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, "new-password");
            pstmt.setLong(2, 1L);
        });

        verify(connection).prepareStatement(sql);
        verify(pstmt).setString(1, "new-password");
        verify(pstmt).setLong(2, 1L);
        verify(pstmt).executeUpdate();
    }

    @Test
    void query는_PreparedStatementSetter로_직접_값을_채울_수_있다() throws SQLException {
        given(rs.next()).willReturn(true, false);
        given(rs.getString("account")).willReturn("gugu");

        final var actual = jdbcTemplate.query(
                "select account from users where id = ?",
                r -> r.getString("account"),
                pstmt -> pstmt.setLong(1, 1L));

        assertThat(actual).containsExactly("gugu");
        verify(pstmt).setLong(1, 1L);
    }

    @Test
    void query는_모든_행을_RowMapper로_변환한다() throws SQLException {
        given(rs.next()).willReturn(true, true, false);
        given(rs.getString("account")).willReturn("gugu", "pobi");
        final var sql = "select account from users";

        final var actual = jdbcTemplate.query(sql, r -> r.getString("account"));

        assertThat(actual).containsExactly("gugu", "pobi");
        verify(connection).prepareStatement(sql);
        verify(rs).close();
    }

    @Test
    void queryForObject는_결과가_하나면_그_값을_반환한다() throws SQLException {
        given(rs.next()).willReturn(true, false);
        given(rs.getString("account")).willReturn("gugu");

        final var actual = jdbcTemplate.queryForObject(
                "select account from users where id = ?", r -> r.getString("account"), 1L);

        assertThat(actual).isEqualTo("gugu");
        verify(pstmt).setObject(1, 1L);
    }

    @Test
    void queryForObject는_결과가_없으면_null을_반환한다() throws SQLException {
        given(rs.next()).willReturn(false);

        final var actual = jdbcTemplate.queryForObject(
                "select account from users where id = ?", r -> r.getString("account"), 1L);

        assertThat(actual).isNull();
    }

    @Test
    void queryForObject는_결과가_여러_개면_예외가_발생한다() throws SQLException {
        given(rs.next()).willReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users", r -> r.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("1개를 초과");
    }

    @Test
    void SQLException이_발생하면_DataAccessException으로_감싸고_자원을_닫는다() throws SQLException {
        final var cause = new SQLException("DB 오류");
        given(pstmt.executeUpdate()).willThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void RowMapper에서_SQLException이_발생해도_원인을_유지하고_모든_자원을_닫는다() throws SQLException {
        final var cause = new SQLException("매핑 오류");
        given(rs.next()).willReturn(true);
        final RowMapper<String> failingRowMapper = r -> {
            throw cause;
        };

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", failingRowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }
}
