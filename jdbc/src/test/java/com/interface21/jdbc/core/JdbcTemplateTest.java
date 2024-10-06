package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.jdbc.support.TestUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    ResultSet rs;
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        PreparedStatement pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("삽입 쿼리를 실행하면 삽입된 데이터를 조회할 수 있다.")
    @Test
    void executeUpdate() throws SQLException {
        // given
        when(rs.next()).thenReturn(true);
        when(rs.getObject("id", Long.class)).thenReturn(1L);
        when(rs.getObject("account", String.class)).thenReturn("ever");

        // when
        String insertSql = "insert into users (account) values (?)";
        jdbcTemplate.executeUpdate(insertSql, "ever");

        // then
        String selectSql = "select id, account from users where id = ?";
        TestUser user = jdbcTemplate.executeQueryForObject(selectSql, TestUser.class, 1);
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getAccount()).isEqualTo("ever");
    }

    @DisplayName("단건 조회 쿼리를 실행하면 해당 데이터를 조회할 수 있다.")
    @Test
    void executeQuery() throws SQLException {
        // given
        when(rs.next()).thenReturn(true);
        when(rs.getObject("id", Long.class)).thenReturn(1L);
        when(rs.getObject("account", String.class)).thenReturn("ever");

        // when
        String selectSql = "select id, account from users where id = ?";
        TestUser user = jdbcTemplate.executeQueryForObject(selectSql, TestUser.class, 1);

        // then
        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getAccount()).isEqualTo("ever");
    }

    @DisplayName("여러건 조회 쿼리를 실행하면 해당 데이터 목록을 조회할 수 있다.")
    @Test
    void executeQueryReturnList() throws SQLException {
        // given
        String insertSql = "insert into users (account) values (?)";
        jdbcTemplate.executeUpdate(insertSql, "ever1");
        jdbcTemplate.executeUpdate(insertSql, "ever2");

        when(rs.next()).thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        // when
        String selectSql = "select id, account from users";
        List<TestUser> testUsers = jdbcTemplate.executeQuery(selectSql, TestUser.class);

        // then
        assertThat(testUsers).hasSize(2);
    }
}
