package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection conn = mock(Connection.class);
    private final PreparedStatement pstmt = mock(PreparedStatement.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private final ResultSet rs = mock(ResultSet.class);

    @Test
    void JdbcTemplate는_SQLException을_DataAccessException으로_바꾼다() throws SQLException {
        String sql = "";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeUpdate()).thenThrow(SQLException.class);
        when(pstmt.executeQuery()).thenThrow(SQLException.class);

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.update(sql)).isInstanceOf(DataAccessException.class),
                () -> assertThatThrownBy(() -> jdbcTemplate.query(sql, (rs1, rowNum) -> "dummy"))
                        .isInstanceOf(DataAccessException.class),
                () -> assertThatThrownBy(
                        () -> jdbcTemplate.queryForObject(sql, (rs1, rowNum) -> "dummy"))
                        .isInstanceOf(DataAccessException.class)
        );
    }

    @Test
    void update_메서드_내부_동작을_테스트한다() throws SQLException {
        String sql = "";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(pstmt);

        jdbcTemplate.update(sql, "arg1", "arg2");

        assertAll(
                () -> verify(dataSource).getConnection(),
                () -> verify(conn).prepareStatement(sql),
                () -> verify(pstmt, times(2)).setObject(anyInt(), any()),
                () -> verify(conn).close(),
                () -> verify(pstmt).close()
        );
    }

    @Test
    void query_메서드_내부_동작을_테스트한다() throws SQLException {
        String sql = "";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<Object> result = jdbcTemplate.query(sql, ((rs1, rowNum) -> "dummy"));

        assertAll(
                () -> assertThat(result).isEmpty(),
                () -> verify(dataSource).getConnection(),
                () -> verify(conn).prepareStatement(sql),
                () -> verify(pstmt, times(0)).setObject(anyInt(), any()),
                () -> verify(rs).next(),
                () -> verify(conn).close(),
                () -> verify(pstmt).close(),
                () -> verify(rs).close()
        );
    }

    @Test
    void queryForObject_메서드_내부_동작을_테스트한다() throws SQLException {
        String sql = "";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true)
                .thenReturn(false);
        when(rs.getRow()).thenReturn(0);

        Object result = jdbcTemplate.queryForObject(sql, ((rs1, rowNum) -> "dummy"));

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> verify(dataSource).getConnection(),
                () -> verify(conn).prepareStatement(sql),
                () -> verify(pstmt, times(0)).setObject(anyInt(), any()),
                () -> verify(rs, times(2)).next(),
                () -> verify(rs).getRow(),
                () -> verify(conn).close(),
                () -> verify(pstmt).close(),
                () -> verify(rs).close()
        );
    }

    @Test
    void queryForObject의_조회_결과가_0개면_예외를_반환한다() throws SQLException {
        String sql = "";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        assertAll(
                () -> assertThatThrownBy(
                        () -> jdbcTemplate.queryForObject(sql, (rs1, rowNum) -> "dummy"))
                        .isInstanceOf(DataAccessException.class)
                        .hasMessage("Incorrect result size: expected 1, actual 0"),
                () -> verify(dataSource).getConnection(),
                () -> verify(conn).prepareStatement(sql),
                () -> verify(pstmt, times(0)).setObject(anyInt(), any()),
                () -> verify(rs).next(),
                () -> verify(conn).close(),
                () -> verify(pstmt).close(),
                () -> verify(rs).close()
        );
    }

    @Test
    void queryForObject의_조회_결과가_2개_이상이면_예외를_반환한다() throws SQLException {
        String sql = "";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(rs.getRow()).thenReturn(0);

        assertAll(
                () -> assertThatThrownBy(
                        () -> jdbcTemplate.queryForObject(sql, (rs1, rowNum) -> "dummy"))
                        .isInstanceOf(DataAccessException.class)
                        .hasMessage("Incorrect result size: expected 1, actual 2"),
                () -> verify(dataSource).getConnection(),
                () -> verify(conn).prepareStatement(sql),
                () -> verify(pstmt, times(0)).setObject(anyInt(), any()),
                () -> verify(rs, times(3)).next(),
                () -> verify(conn).close(),
                () -> verify(pstmt).close(),
                () -> verify(rs).close()
        );
    }
}