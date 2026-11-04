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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {

    private static final RowMapper<String> NAME_ROW_MAPPER = (rs, rowNum) -> rs.getString("name");

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
    void 가변인자를_순서대로_바인딩하고_변경된_행의_수를_반환한다() throws SQLException {
        given(pstmt.executeUpdate()).willReturn(1);

        final int updated = jdbcTemplate.update("update users set account = ? where id = ?", "gugu", 1L);

        assertThat(updated).isEqualTo(1);
        verify(pstmt).setObject(1, "gugu");
        verify(pstmt).setObject(2, 1L);
    }

    @Test
    void PreparedStatementSetter로_값을_직접_바인딩할_수_있다() throws SQLException {
        jdbcTemplate.update("delete from users where id = ?", statement -> statement.setLong(1, 1L));

        verify(pstmt).setLong(1, 1L);
        verify(pstmt).executeUpdate();
    }

    @Test
    void 조회_결과를_RowMapper로_변환하고_행_번호를_0부터_전달한다() throws SQLException {
        given(rs.next()).willReturn(true, true, false);

        final List<Integer> rowNums = jdbcTemplate.query("select id from users", (resultSet, rowNum) -> rowNum);

        assertThat(rowNums).containsExactly(0, 1);
    }

    @Test
    void PreparedStatementSetter와_RowMapper를_함께_전달해_조회할_수_있다() throws SQLException {
        given(rs.next()).willReturn(true, false);
        given(rs.getString("name")).willReturn("gugu");

        final List<String> names = jdbcTemplate.query(
                "select name from users where id = ?",
                statement -> statement.setLong(1, 1L),
                NAME_ROW_MAPPER);

        assertThat(names).containsExactly("gugu");
        verify(pstmt).setLong(1, 1L);
    }

    @Test
    void 단건_조회_결과를_반환한다() throws SQLException {
        given(rs.next()).willReturn(true, false);
        given(rs.getString("name")).willReturn("gugu");

        final String name = jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L);

        assertThat(name).isEqualTo("gugu");
        verify(pstmt).setObject(1, 1L);
    }

    @Test
    void 단건_조회_결과가_없으면_null을_반환한다() throws SQLException {
        given(rs.next()).willReturn(false);

        final String name = jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L);

        assertThat(name).isNull();
    }

    @Test
    void 단건_조회_결과가_여러_개면_예외가_발생한다() throws SQLException {
        given(rs.next()).willReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from users", NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void SQLException은_DataAccessException으로_변환된다() throws SQLException {
        final SQLException cause = new SQLException("실패");
        given(pstmt.executeUpdate()).willThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("update users set account = ?", "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    void 조회가_끝나면_모든_자원을_닫는다() throws SQLException {
        given(rs.next()).willReturn(false);

        jdbcTemplate.query("select name from users", NAME_ROW_MAPPER);

        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void 예외가_발생해도_자원을_닫는다() throws SQLException {
        given(pstmt.executeUpdate()).willThrow(new SQLException("실패"));

        assertThatThrownBy(() -> jdbcTemplate.update("update users set account = ?", "gugu"))
                .isInstanceOf(DataAccessException.class);

        verify(pstmt).close();
        verify(connection).close();
    }
}
