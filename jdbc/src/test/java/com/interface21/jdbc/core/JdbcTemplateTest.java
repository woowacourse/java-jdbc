package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any())).thenReturn(pstmt);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void 데이터_생성_성공() {
        TestUser user = new TestUser("jojo", "1234");
        String sql = "insert into test-user (account, password) values (?, ?)";

        jdbcTemplate.update(sql, user.getAccount(), user.getPassword());

        assertAll(
                () -> verify(pstmt).setObject(1, user.getAccount()),
                () -> verify(pstmt).setObject(2, user.getPassword()),
                () -> verify(pstmt, times(1)).executeUpdate(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close()
        );
    }

    @Test
    void 데이터_생성_예외_발생() throws SQLException {
        TestUser user = new TestUser("jojo", "1234");
        String sql = "insert into test-user (account, password) values (?, ?)";

        when(pstmt.executeUpdate()).thenThrow(SQLException.class);

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.update(sql, user.getAccount(), user.getPassword()))
                        .isInstanceOf(DataAccessException.class),
                () -> verify(pstmt).setObject(1, user.getAccount()),
                () -> verify(pstmt).setObject(2, user.getPassword()),
                () -> verify(pstmt, times(1)).executeUpdate(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close()
        );
    }

    @Test
    void 단일_데이터_조회_성공() throws SQLException {
        TestUser user = new TestUser("jojo", "1234");
        String sql = "select id, account, password from users where account = ?";

        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("account")).thenReturn("jojo");
        when(rs.getString("password")).thenReturn("1234");

        TestUser actual = jdbcTemplate.query(sql, this::createTestUser, user.getAccount());

        assertAll(
                () -> assertThat(actual).isNotNull().extracting(TestUser::getAccount).isEqualTo(user.getAccount()),
                () -> verify(pstmt).setObject(1, user.getAccount()),
                () -> verify(pstmt, times(1)).executeQuery(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close(),
                () -> verify(rs, times(1)).close()
        );
    }

    @Test
    void 단일_데이터_조회_에러_발생() throws SQLException {
        TestUser user = new TestUser("jojo", "1234");
        String sql = "select id, account, password from users where account = ?";

        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenThrow(SQLException.class);

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.query(sql, this::createTestUser, user.getAccount()))
                        .isInstanceOf(DataAccessException.class),
                () -> verify(pstmt).setObject(1, user.getAccount()),
                () -> verify(pstmt, times(1)).executeQuery(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close(),
                () -> verify(rs, times(1)).close()
        );
    }

    @Test
    void 단일_데이터_조회_실패() throws SQLException {
        TestUser user = new TestUser("jojo", "1234");
        String sql = "select id, account, password from users where account = ?";

        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        TestUser actual = jdbcTemplate.query(sql, this::createTestUser, user.getAccount());

        assertAll(
                () -> assertThat(actual).isNull(),
                () -> verify(pstmt).setObject(1, user.getAccount()),
                () -> verify(pstmt, times(1)).executeQuery(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close(),
                () -> verify(rs, times(1)).close()
        );
    }

    @Test
    void 복수_데이터_조회_성공() throws SQLException {
        String sql = "select id, account, password from users";

        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("account")).thenReturn("jojo", "cutehuman");
        when(rs.getString("password")).thenReturn("jojo1234", "cutehuman1234");

        List<TestUser> actual = jdbcTemplate.query(sql, this::createTestUsers);

        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual.get(0).getAccount()).isEqualTo("jojo"),
                () -> assertThat(actual.get(1).getAccount()).isEqualTo("cutehuman"),
                () -> verify(pstmt, times(1)).executeQuery(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close(),
                () -> verify(rs, times(1)).close()
        );
    }

    @Test
    void 복수_데이터_조회_에러_발생() throws SQLException {
        String sql = "select id, account, password from users";

        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenThrow(SQLException.class);

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.query(sql, this::createTestUsers))
                        .isInstanceOf(DataAccessException.class),
                () -> verify(pstmt, times(1)).executeQuery(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close(),
                () -> verify(rs, times(1)).close()
        );
    }

    @Test
    void 복수_데이터_조회_실패() throws SQLException {
        String sql = "select id, account, password from users";

        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<TestUser> actual = jdbcTemplate.query(sql, this::createTestUsers);

        assertAll(
                () -> assertThat(actual).isNull(),
                () -> verify(pstmt, times(1)).executeQuery(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close(),
                () -> verify(rs, times(1)).close()
        );
    }

    private List<TestUser> createTestUsers(ResultSet rs) throws SQLException {
        List<TestUser> users = new ArrayList<>();
        while (rs.next()) {
            users.add(createTestUser(rs));
        }
        return users;
    }

    private TestUser createTestUser(ResultSet rs) throws SQLException {
        return new TestUser(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password")
        );
    }

    static class TestUser {

        private final Long id;
        private final String account;
        private final String password;

        public TestUser(String account, String password) {
            this(null, account, password);
        }

        public TestUser(Long id, String account, String password) {
            this.id = id;
            this.account = account;
            this.password = password;
        }

        public String getAccount() {
            return account;
        }

        public String getPassword() {
            return password;
        }
    }
}
