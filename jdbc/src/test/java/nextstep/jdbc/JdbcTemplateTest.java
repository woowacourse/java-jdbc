package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.core.RowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    public static final RowMapper<DummyUser> DUMMY_USER_ROW_MAPPER = (rs) -> new DummyUser(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private Connection conn;
    private JdbcTemplate jdbcTemplate;
    private PreparedStatement pstmt;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(conn);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("update가 성공한다.")
    @Test
    void update() throws SQLException {
        final var sql = "update users set account = ? where id = ?";
        when(conn.prepareStatement(sql)).thenReturn(pstmt);

        assertDoesNotThrow(() -> jdbcTemplate.update(sql, 1, 2));
    }

    @DisplayName("반환할 객체가 없으면 null을 반환한다.")
    @Test
    void queryForObjectWhenNotFound() throws SQLException {
        final var sql = "select * from users where id = ?";
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);
        DummyUser dummyUser = jdbcTemplate.queryForObject(sql, DUMMY_USER_ROW_MAPPER, 1);

        assertThat(dummyUser).isNull();
    }

    @DisplayName("1개의 객체를 반환한다.")
    @Test
    void queryForObject() throws SQLException {
        final var sql = "select * from users where id = ?";
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        DummyUser dummyUser = jdbcTemplate.queryForObject(sql, DUMMY_USER_ROW_MAPPER, 1);

        assertThat(dummyUser).isNotNull();
    }

    @DisplayName("1개이상의 객체를 반환하면 null을 반환한다.")
    @Test
    void queryForObjectFailure() throws SQLException {
        final var sql = "select * from users where id = ?";
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.getRow()).thenReturn(2);
        DummyUser dummyUser = jdbcTemplate.queryForObject(sql, DUMMY_USER_ROW_MAPPER);

        assertThat(dummyUser).isNull();
    }

    @DisplayName("반환할 객체가 없으면 빈 리스트를 반환한다.")
    @Test
    void queryForEmpty() throws SQLException {
        final var sql = "select * from users";
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);
        List<DummyUser> dummyUsers = jdbcTemplate.query(sql, DUMMY_USER_ROW_MAPPER);

        assertThat(dummyUsers).isEmpty();
    }

    @DisplayName("2개 이상의 객체를 반환할 수 있다.")
    @Test
    void query() throws SQLException {
        final var sql = "select * from users";
        when(conn.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true)
                .thenReturn(true, false);

        List<DummyUser> dummyUsers = jdbcTemplate.query(sql, DUMMY_USER_ROW_MAPPER);

        assertThat(dummyUsers).hasSize(3);
    }
}
