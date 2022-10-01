package nextstep.jdbc;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    @Test
    void update_메서드_내부_동작을_테스트한다() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(pstmt);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.update(sql, "ohzzi", "password", "ohzzi@woowahan.com");

        assertAll(
                () -> verify(dataSource).getConnection(),
                () -> verify(conn).prepareStatement(sql),
                () -> verify(pstmt, times(3)).setObject(anyInt(), any())
        );
    }

    @Test
    void update_메서드는_SQLException을_RuntimeException으로_바꾼다() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        String sql = "sql";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeUpdate()).thenThrow(SQLException.class);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        assertThatThrownBy(() -> jdbcTemplate.update(sql)).isExactlyInstanceOf(DataAccessException.class);
    }
}