package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;
    private ParameterMetaData parameterMetaData;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
        parameterMetaData = mock(ParameterMetaData.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.getParameterMetaData()).thenReturn(parameterMetaData);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void 데이터_생성_성공() throws SQLException {
        TestUser user = new TestUser("jojo", "1234");
        String sql = "insert into test-user (account, password) values (?, ?)";

        when(parameterMetaData.getParameterCount()).thenReturn(2);

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
    void 데이터_생성시_파라미터_바인딩_예외_발생() throws SQLException {
        TestUser user = new TestUser("jojo", "1234");
        String sql = "insert into test-user (account, password) values (?, ?)";

        when(parameterMetaData.getParameterCount()).thenReturn(3);

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.update(sql, user.getAccount(), user.getPassword()))
                        .isInstanceOf(DataAccessException.class),
                () -> verify(pstmt, never()).setObject(anyInt(), anyString()),
                () -> verify(pstmt, never()).executeUpdate(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close()
        );
    }

    @Test
    void 단일_데이터_조회_성공() throws SQLException {
        TestUser user = new TestUser("jojo", "1234");
        String sql = "select id, account, password from users where account = ?";

        when(parameterMetaData.getParameterCount()).thenReturn(1);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("account")).thenReturn("jojo");
        when(rs.getString("password")).thenReturn("1234");

        Optional<TestUser> actual = jdbcTemplate.queryForObject(sql, this::createTestUser, user.getAccount());

        assertAll(
                () -> assertThat(actual).isPresent()
                        .get().extracting(TestUser::getAccount).isEqualTo(user.getAccount()),
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

        when(parameterMetaData.getParameterCount()).thenReturn(1);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getLong("id")).thenThrow(SQLException.class);

        assertAll(
                () -> assertThatThrownBy(
                        () -> jdbcTemplate.queryForObject(sql, this::createTestUser, user.getAccount()))
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

        when(parameterMetaData.getParameterCount()).thenReturn(1);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Optional<TestUser> actual = jdbcTemplate.queryForObject(sql, this::createTestUser, user.getAccount());

        assertAll(
                () -> assertThat(actual).isEmpty(),
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

        when(parameterMetaData.getParameterCount()).thenReturn(0);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("account")).thenReturn("jojo", "cutehuman");
        when(rs.getString("password")).thenReturn("jojo1234", "cutehuman1234");

        List<TestUser> actual = jdbcTemplate.query(sql, this::createTestUser);

        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual.getFirst().getAccount()).isEqualTo("jojo"),
                () -> assertThat(actual.getLast().getAccount()).isEqualTo("cutehuman"),
                () -> verify(pstmt, times(1)).executeQuery(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close(),
                () -> verify(rs, times(1)).close()
        );
    }

    @Test
    void 복수_데이터_조회_에러_발생() throws SQLException {
        String sql = "select id, account, password from users";

        when(parameterMetaData.getParameterCount()).thenReturn(0);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenThrow(SQLException.class);

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.query(sql, this::createTestUser))
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

        when(parameterMetaData.getParameterCount()).thenReturn(0);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        List<TestUser> actual = jdbcTemplate.query(sql, this::createTestUser);

        assertAll(
                () -> assertThat(actual).isEmpty(),
                () -> verify(pstmt, times(1)).executeQuery(),
                () -> verify(pstmt, times(1)).close(),
                () -> verify(conn, times(1)).close(),
                () -> verify(rs, times(1)).close()
        );
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
