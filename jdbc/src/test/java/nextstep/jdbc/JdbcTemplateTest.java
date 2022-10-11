package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<UserObject> OBJECT_ROW_MAPPER = rs -> new UserObject(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        // given
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);

        given(dataSource.getConnection()).willReturn(conn);
        given(conn.prepareStatement(any())).willReturn(pstmt);

        // when
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        String account = "brorae";
        String password = "password";
        String email = "brorae@woowa.com";
        jdbcTemplate.update(sql, account, password, email);

        // then
        verify(pstmt).setObject(1, account);
        verify(pstmt).setObject(2, password);
        verify(pstmt).setObject(3, email);
        verify(pstmt).executeUpdate();
        verify(conn).close();
        verify(pstmt).close();
    }

    @Test
    void queryForObject() throws SQLException {
        // given
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(conn);
        given(conn.prepareStatement(any())).willReturn(pstmt);
        given(pstmt.executeQuery()).willReturn(rs);
        given(rs.next()).willReturn(true, false);

        // when
        String sql = "select id, account, password, email from users where id = ?";
        UserObject user = jdbcTemplate.queryForObject(sql, OBJECT_ROW_MAPPER, 1L);

        // then
        assertThat(user).isNotNull();
        verify(conn).close();
        verify(pstmt).close();
    }

    @Test
    void queryForObjectWithNoDataThrowException() throws SQLException {
        // given
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(conn);
        given(conn.prepareStatement(any())).willReturn(pstmt);
        given(pstmt.executeQuery()).willReturn(rs);
        given(rs.next()).willReturn(false);

        // when & then
        String sql = "select id, account, password, email from users where id = ?";
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, OBJECT_ROW_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void queryForObjectWithMoreThanTwoThrowException() throws SQLException {
        // given
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(conn);
        given(conn.prepareStatement(any())).willReturn(pstmt);
        given(pstmt.executeQuery()).willReturn(rs);
        given(rs.next()).willReturn(true, true, false);

        // when & then
        String sql = "select id, account, password, email from users where id = ?";
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, OBJECT_ROW_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void query() throws SQLException {
        // given
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(conn);
        given(conn.prepareStatement(any())).willReturn(pstmt);
        given(pstmt.executeQuery()).willReturn(rs);
        given(rs.next()).willReturn(true, true, false);

        // when
        String sql = "select id, account, password, email from users";
        List<UserObject> users = jdbcTemplate.query(sql, OBJECT_ROW_MAPPER);

        // then
        assertThat(users).hasSize(2);
        verify(conn).close();
        verify(pstmt).close();
    }
}
