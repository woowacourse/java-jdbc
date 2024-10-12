package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.interface21.dao.IncorrectResultSizeException;

class JdbcTemplateTest {

    private static final RowMapper<User> ROW_MAPPER = (rs) -> new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getInt("age"));

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate();
    }

    @Nested
    @DisplayName("query")
    class Query {
        @Test
        @DisplayName("리스트를 반환한다.")
        void returnsList() throws SQLException {
            // given
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getInt("age")).thenReturn(20, 25);

            // when
            List<User> users = jdbcTemplate.query(
                    connection,
                    "select * from users where age = ?",
                    ROW_MAPPER,
                    18
            );

            // then
            verify(preparedStatement).executeQuery();
            verify(preparedStatement).setObject(1, 18);
            assertThat(users).hasSize(2);
            assertThat(users.get(0).getAge()).isEqualTo(20);
            assertThat(users.get(1).getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("파라미터를 전달하지 않은 경우에도 동작한다.")
        void withoutParam() throws SQLException {
            // given
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getString("name")).thenReturn("user 1", "user 2");

            // when
            List<User> users = jdbcTemplate.query(
                    connection,
                    "select * from users",
                    ROW_MAPPER
            );

            // then
            verify(preparedStatement).executeQuery();
            verify(preparedStatement, never()).setObject(anyInt(), anyString());
            assertThat(users).hasSize(2);
            assertThat(users.get(0).getName()).isEqualTo("user 1");
            assertThat(users.get(1).getName()).isEqualTo("user 2");
        }
    }

    @Nested
    @DisplayName("queryForObject")
    class QueryForObject {
        @Test
        @DisplayName("객체를 반환한다.")
        void returnObject() throws SQLException {
            // given
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("name")).thenReturn("user 1");

            // when
            User user = jdbcTemplate.queryForObject(
                    connection,
                    "select * from users where id = ?",
                    ROW_MAPPER,
                    1
            );

            // then
            verify(preparedStatement).executeQuery();
            verify(preparedStatement).setObject(1, 1);
            assertThat(user.getName()).isEqualTo("user 1");
        }

        @Test
        @DisplayName("파라미터를 전달하지 않은 경우에도 동작한다.")
        void withoutParam() throws SQLException {
            // given
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("name")).thenReturn("user 1");

            // when
            User user = jdbcTemplate.queryForObject(
                    connection,
                    "select * from users where id = 1",
                    ROW_MAPPER
            );

            // then
            verify(preparedStatement).executeQuery();
            verify(preparedStatement, never()).setObject(anyInt(), anyInt());
            assertThat(user.getName()).isEqualTo("user 1");
        }

        @Test
        @DisplayName("결과값이 여러개인 경우 IncorrectResultSizeException을 던진다.")
        void incorrectResultSizeException() throws SQLException {
            // given
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getString("name")).thenReturn("user 1", "user 2");

            // when & then
            assertThatThrownBy(
                    () -> jdbcTemplate.queryForObject(connection, "select * from users where id = ?", ROW_MAPPER, 1))
                    .isInstanceOf(IncorrectResultSizeException.class)
                    .hasMessage("Expected: 1, but actual: 2");
        }
    }

    @Test
    @DisplayName("update 관련 쿼리 테스트")
    void update() throws SQLException {
        // given
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        // when
        jdbcTemplate.update(
                connection,
                "insert into users (account, password, email) values (?, ?, ?)",
                "account",
                "password",
                "email@email.com");

        // then
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).setObject(1, "account");
        verify(preparedStatement).setObject(2, "password");
        verify(preparedStatement).setObject(3, "email@email.com");
    }

    private static class User {

        private Long id;
        private String name;
        private int age;

        public User(long id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

}
