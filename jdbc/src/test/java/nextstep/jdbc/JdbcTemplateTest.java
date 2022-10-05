package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection conn = mock(Connection.class);
    private final PreparedStatement pstmt = mock(PreparedStatement.class);
    private final ResultSet rs = mock(ResultSet.class);
    private JdbcTemplate jdbcTemplate;

    private static RowMapper<User> rowMapper() {
        return (rs) -> new User(
                rs.getLong("id"),
                rs.getString("account")
        );
    }

    @BeforeEach
    void setUp() throws SQLException {
        jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection()).willReturn(conn);
        given(conn.prepareStatement(anyString())).willReturn(pstmt);
        given(pstmt.executeQuery()).willReturn(rs);
    }

    @Test
    @DisplayName("update 메서드를 실행한다.")
    void update() throws SQLException {
        final String sql = "insert into users (id, account) values (?, ?)";

        jdbcTemplate.query(sql, rowMapper(), 1L, "kth990303");

        verify(pstmt, times(1)).setObject(1, 1L);
        verify(pstmt, times(1)).setObject(2, "kth990303");
        verify(pstmt).close();
        verify(conn).close();
    }

    @Test
    @DisplayName("queryForObject 메서드를 실행한다.")
    void queryForObject() throws SQLException {
        final String sql = "select id, account from users where id = ?";

        jdbcTemplate.query(sql, rowMapper(), 1L);

        verify(pstmt, times(1)).executeQuery();
        verify(pstmt).close();
        verify(conn).close();
    }

    @Test
    @DisplayName("query 메서드를 실행한다.")
    void query() throws SQLException {
        final String sql = "select id, account from users";

        jdbcTemplate.query(sql, rowMapper());

        verify(pstmt, times(1)).executeQuery();
        verify(pstmt).close();
        verify(conn).close();
    }

    static class User {
        private final Long id;
        private final String account;

        public User(final Long id, final String account) {
            this.id = id;
            this.account = account;
        }

        public Long getId() {
            return id;
        }

        public String getAccount() {
            return account;
        }
    }
}
