package nextstep.jdbc;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

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
                () -> assertThatThrownBy(() -> jdbcTemplate.update(sql)).isExactlyInstanceOf(DataAccessException.class),
                () -> assertThatThrownBy(() -> jdbcTemplate.query(sql, new Object[0], (rs1, rowNum) -> "dummy"))
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

        List<String> result = jdbcTemplate.query(sql, new Object[0], ((rs1, rowNum) -> "dummy"));

        assertAll(
                () -> assertThat(result).isEmpty(),
                () -> verify(dataSource).getConnection(),
                () -> verify(conn).prepareStatement(sql),
                () -> verify(pstmt, times(0)).setObject(anyInt(), any()),
                () -> verify(rs).next()
        );
    }
}