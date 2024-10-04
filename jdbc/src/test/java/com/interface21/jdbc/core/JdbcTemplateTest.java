package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private Connection conn;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private JdbcTemplate jdbcTemplate;
    private UserRowMapper userRowMapper;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = Mockito.mock(DataSource.class);

        conn = Mockito.mock(Connection.class);
        pstmt = Mockito.mock(PreparedStatement.class);
        rs = Mockito.mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(any(String.class))).thenReturn(pstmt);

        jdbcTemplate = new JdbcTemplate(dataSource);
        userRowMapper = new UserRowMapper();

        when(pstmt.executeQuery()).thenReturn(rs);
    }

    @AfterEach
    void tearDown() throws SQLException {
        verify(conn).close();
        verify(pstmt).close();
    }

    @DisplayName("변경 쿼리를 실행할 수 있다.")
    @Test
    void update() {
        jdbcTemplate.update("insert into users values (?, ?)", 1L, "jerry");

        assertAll(
                () -> verify(pstmt).setObject(1, 1L),
                () -> verify(pstmt).setObject(2, "jerry"),
                () -> verify(pstmt).executeUpdate()
        );
    }

    @Nested
    class queryForObject {

        private final String sql = "select * from users where name = ?";

        @DisplayName("단건 조회 쿼리를 실행할 수 있다.")
        @Test
        void single() throws SQLException {
            when(rs.next()).thenReturn(true, false);
            when(rs.getLong("id")).thenReturn(1L);
            when(rs.getString("name")).thenReturn("jerry");

            final var actual = jdbcTemplate.queryForObject(sql, userRowMapper, "jerry");

            assertAll(
                    () -> assertThat(actual).isEqualTo(new User(1L, "jerry")),
                    () -> verify(rs).close()
            );
        }

        @DisplayName("결과가 없으면 예외가 발생한다.")
        @Test
        void empty() throws SQLException {
            when(rs.next()).thenReturn(false);

            assertAll(
                    () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, userRowMapper, "jerry"))
                            .isInstanceOf(EmptyResultDataAccessException.class),
                    () -> verify(rs).close()
            );
        }

        @DisplayName("여러 건의 결과가 있으면 예외가 발생한다.")
        @Test
        void multiple() throws SQLException {
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getLong("id")).thenReturn(1L, 2L);
            when(rs.getString("name")).thenReturn("jerry", "jerry");

            assertAll(
                    () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, userRowMapper, "jerry"))
                            .isInstanceOf(IncorrectResultSizeDataAccessException.class),
                    () -> verify(rs).close()
            );
        }
    }

    @DisplayName("여러 건 조회 쿼리를 실행할 수 있다.")
    @Test
    void query() throws SQLException {
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("name")).thenReturn("jerry", "myeongoh");

        final var actual = jdbcTemplate.query("select * from users", userRowMapper);

        assertAll(
                () -> assertThat(actual).containsExactly(new User(1L, "jerry"), new User(2L, "myeongoh")),
                () -> verify(rs).close()
        );
    }

    private record User(Long id, String name) {

    }

    private static class UserRowMapper implements RowMapper<User> {

        @Override
        public User mapRow(ResultSet rs) throws SQLException {
            return new User(
                    rs.getLong("id"),
                    rs.getString("name")
            );
        }
    }
}

