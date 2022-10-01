package nextstep.jdbc;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        when(conn.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)).thenReturn(pstmt);
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
                () -> verify(pstmt, times(2)).setObject(anyInt(), any())
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
                () -> verify(rs).next()
        );
    }

    @Test
    void queryForObject_메서드_내부_동작을_테스트한다() throws SQLException {
        String sql = "";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.last()).thenReturn(true);
        when(rs.getRow()).thenReturn(1);
        doNothing().when(rs)
                .beforeFirst();
        when(rs.next()).thenReturn(true);

        Object result = jdbcTemplate.queryForObject(sql, ((rs1, rowNum) -> "dummy"));

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> verify(dataSource).getConnection(),
                () -> verify(conn).prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY),
                () -> verify(pstmt, times(0)).setObject(anyInt(), any()),
                () -> verify(rs).next(),
                () -> verify(rs).getRow()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void queryForObject의_조회_결과가_1개가_아니면_예외를_반환한다(final int resultSetSize) throws SQLException {
        String sql = "";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.last()).thenReturn(true);
        when(rs.getRow()).thenReturn(resultSetSize);
        doNothing().when(rs)
                .beforeFirst();
        when(rs.next()).thenReturn(resultSetSize > 0);

        assertAll(
                () -> assertThatThrownBy(
                        () -> jdbcTemplate.queryForObject(sql, (rs1, rowNum) -> "dummy"))
                        .isInstanceOf(DataAccessException.class),
                () -> verify(dataSource).getConnection(),
                () -> verify(conn).prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY),
                () -> verify(pstmt, times(0)).setObject(anyInt(), any()),
                () -> verify(rs).getRow()
        );
    }
}