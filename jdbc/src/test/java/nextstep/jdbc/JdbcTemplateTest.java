package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class JdbcTemplateTest {

    @Mock
    private Connection conn;
    @Mock
    private DataSource dataSource;
    @Mock
    private PreparedStatement pstmt;
    @Mock
    private ResultSet rs;

    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> userRowMapper = (rs ->
        new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
        )
    );

    private static class User {

        private final Long id;
        private final String account;
        private final String password;
        private final String email;

        public User(Long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }


    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        jdbcTemplate = new JdbcTemplate(dataSource);
        given(dataSource.getConnection()).willReturn(conn);
        given(conn.prepareStatement(anyString())).willReturn(pstmt);
        given(pstmt.executeQuery()).willReturn(rs);
        given(pstmt.executeQuery(anyString())).willReturn(rs);
        given(pstmt.getConnection()).willReturn(conn);

    }

    @DisplayName("insert 쿼리")
    @Test
    void insert() throws SQLException {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        String account = "account";
        String password = "password";
        String email = "email";

        jdbcTemplate.update(sql, account, password, email);

        verify(pstmt).setObject(1, account);
        verify(pstmt).setObject(2, password);
        verify(pstmt).setObject(3, email);
        verify(pstmt).close();
        verify(conn).close();
    }

    @DisplayName("update 쿼리")
    @Test
    void update() throws SQLException {
        String sql = "update users set account = ?, email = ? where id = ?";
        String account = "update";
        String email = "update@email.com";
        Long id = 1L;

        jdbcTemplate.update(sql, account, email, id);

        verify(pstmt).setObject(1, account);
        verify(pstmt).setObject(2, email);
        verify(pstmt).setObject(3, id);
        verify(pstmt).close();
        verify(conn).close();
    }

    @DisplayName("queryForObject 동작 확인")
    @Test
    void queryForObject() throws SQLException {
        given(rs.next()).willReturn(true, false);
        given(rs.getLong("id")).willReturn(1L);
        given(rs.getString("account")).willReturn("account");
        given(rs.getString("password")).willReturn("password");
        given(rs.getString("email")).willReturn("email");

        String sql = "select id, account, password, email from users where id = ?";
        User findUser = jdbcTemplate.queryForObject(sql, userRowMapper, 1L);

        assertThat(findUser.getEmail()).isEqualTo("email");
        verify(pstmt).setObject(1, 1L);
        verify(pstmt).close();
        verify(conn).close();
        verify(rs).close();
    }

    @Test
    void findAll() throws SQLException {
        given(rs.next()).willReturn(true, true, false);

        String sql = "select * from users";

        List<User> users = jdbcTemplate.query(sql, userRowMapper);

        assertThat(users).hasSize(2);
        verify(pstmt).executeQuery();
        verify(pstmt).close();
        verify(rs).close();
        verify(conn).close();
    }

}