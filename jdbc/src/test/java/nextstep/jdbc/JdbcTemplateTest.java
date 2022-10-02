package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    @Test
    void update_query를_진행한다() throws SQLException {
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        String sql = "insert query";
        int effectedQuery = jdbcTemplate.update(sql, "eden", "password", "eden@eden.com");

        // then
        assertThat(effectedQuery).isEqualTo(1);
    }

    @Test
    void query_for_object를_진행한다() throws SQLException {
        class TestUser {

        }
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        String sql = "insert query";
        TestUser testUser = jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> new TestUser(), "eden");

        // then
        assertThat(testUser).isNotNull();
    }

    @Test
    void query_for_list를_진행한다() throws SQLException {
        class TestUser {

        }
        // given
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        String sql = "insert query";
        List<TestUser> testUser = jdbcTemplate.queryForList(sql, (resultSet, rowNum) -> new TestUser(), "eden");

        // then
        assertThat(testUser).hasSize(2);
    }
}
