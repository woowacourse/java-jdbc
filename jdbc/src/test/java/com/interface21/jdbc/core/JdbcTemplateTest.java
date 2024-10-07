package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    JdbcTemplate jdbcTemplate;
    PreparedStatement pstm;

    @BeforeEach
    void setUp() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        pstm = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstm);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update가_정상_작동한다() throws SQLException {
        // given
        String sql = "INSERT INTO user (id, name) VALUES (?, ?)";

        // when
        jdbcTemplate.update(sql, 1L, "userA");

        // thenR
        verify(pstm).setObject(1, 1L);
        verify(pstm).setObject(2, "userA");
    }

    @Test
    void query가_정상_작동한다() throws SQLException {
        // given
        ResultSet rs = mock(ResultSet.class);
        when(pstm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("name")).thenReturn("userA");

        // when
        List<TestUser> users = jdbcTemplate.query("SELECT id, name WHERE id = ?", testUserRowMapper(), 1L);

        // then
        Assertions.assertAll(
                () -> assertThat(users).hasSize(1),
                () -> assertThat(users.get(0)).isEqualTo(new TestUser(1L, "userA"))
        );
    }

    @Test
    void queryForObject가_정상_작동한다() throws SQLException {
        // given
        ResultSet rs = mock(ResultSet.class);
        when(pstm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("name")).thenReturn("userA");

        // when
        TestUser user = jdbcTemplate.queryForObject("SELECT id, name WHERE id = ?", testUserRowMapper(), 1L);

        // then
        assertThat(user).isEqualTo(new TestUser(1L, "userA"));
    }

    class TestUser {
        private Long id;
        private String name;

        public TestUser(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            TestUser testUser = (TestUser) object;
            return Objects.equals(id, testUser.id) && Objects.equals(name, testUser.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }

    private RowMapper<TestUser> testUserRowMapper() {
        return (rs) -> {
            return new TestUser(rs.getLong("id"), rs.getString("name"));
        };
    }
}
