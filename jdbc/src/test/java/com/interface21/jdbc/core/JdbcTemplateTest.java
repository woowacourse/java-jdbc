package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Nested
    @DisplayName("update 메서드 테스트")
    class Update {

        private static final int EXPECTED = 1;
        private static final String ACCOUNT = "kyummi";
        private static final String PASSWORD = "password";
        private static final String EMAIL = "kyum@naver.com";

        @BeforeEach
        void setUp() throws SQLException {
            when(statement.executeUpdate()).thenReturn(EXPECTED);
        }

        @Test
        @DisplayName("INSERT 문, 데이터 추가 성공")
        void insert() throws SQLException {
            // given
            var sql = "insert into users (account, password, email) values (?, ?, ?)";
            when(connection.prepareStatement(sql)).thenReturn(statement);

            // when
            var actual = jdbcTemplate.update(sql, ACCOUNT, PASSWORD, EMAIL);

            // then
            assertAll(
                    () -> assertThat(actual).isEqualTo(EXPECTED),
                    () -> verify(connection).prepareStatement(sql),
                    () -> verify(statement).setObject(1, ACCOUNT),
                    () -> verify(statement).setObject(2, PASSWORD),
                    () -> verify(statement).setObject(3, EMAIL),
                    () -> verify(statement).executeUpdate()
            );
        }

        @Test
        @DisplayName("UPDATE 문, 데이터 수정 성공")
        void update() throws SQLException {
            // given
            var sql = "update users set account = ?, password = ?, email = ?";
            when(connection.prepareStatement(sql)).thenReturn(statement);

            // when
            var actual = jdbcTemplate.update(sql, ACCOUNT, PASSWORD, EMAIL);

            // then
            assertAll(
                    () -> assertThat(actual).isEqualTo(EXPECTED),
                    () -> verify(connection).prepareStatement(sql),
                    () -> verify(statement).setObject(1, ACCOUNT),
                    () -> verify(statement).setObject(2, PASSWORD),
                    () -> verify(statement).setObject(3, EMAIL),
                    () -> verify(statement).executeUpdate()
            );
        }
    }

    @Nested
    @DisplayName("query 메서드 테스트")
    class Query {

        private static final User FIRST_USER = new User(1L, "kyummi", "password", "kyum@naver.com");
        private static final User SECOND_USER = new User(2L, "kiki", "password", "kikinaver.com");

        private void setResultSet() throws SQLException {
            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.next()).thenReturn(true,true, false);

            when(resultSet.getLong("id")).thenReturn(FIRST_USER.id(), SECOND_USER.id());
            when(resultSet.getString("account")).thenReturn(FIRST_USER.account(), SECOND_USER.account());
            when(resultSet.getString("password")).thenReturn(FIRST_USER.password(), SECOND_USER.password());
            when(resultSet.getString("email")).thenReturn(FIRST_USER.email(), SECOND_USER.email());

            when(statement.executeQuery()).thenReturn(resultSet);
        }

        private RowMapper<User> getRowMapper() {
            return (rs, size) ->
                    new User(
                            rs.getLong("id"),
                            rs.getString("account"),
                            rs.getString("password"),
                            rs.getString("email"));
        }

        @Test
        @DisplayName("SELECT 문, 데이터 검색 성공 (2개)")
        void query() throws SQLException {
            // given
            var sql = "select id, account, password, email from users";
            when(connection.prepareStatement(sql)).thenReturn(statement);
            setResultSet();

            // when
            var actual = jdbcTemplate.query(sql, getRowMapper());

            // then
            assertAll(
                    () -> assertThat(actual).hasSize(2),
                    () -> assertThat(actual).containsExactly(FIRST_USER, SECOND_USER),
                    () -> verify(connection).prepareStatement(sql),
                    () -> verify(statement, never()).setObject(anyInt(), any()),
                    () -> verify(statement).executeQuery()
            );
        }

        @Test
        @DisplayName("SELECT 문, 데이터 검색 성공 (1개)")
        void queryObject() throws SQLException {
            // given
            var sql = "select id, account, password, email from users where id = ?";
            when(connection.prepareStatement(sql)).thenReturn(statement);
            setResultSet();
            var id = 1L;

            // when
            var actual = jdbcTemplate.queryObject(sql, getRowMapper(), id);

            // then
            assertAll(
                    () -> assertThat(actual).isEqualTo(FIRST_USER),
                    () -> verify(connection).prepareStatement(sql),
                    () -> verify(statement).setObject(1, id),
                    () -> verify(statement).executeQuery()
            );
        }
    }

    record User(Long id, String account, String password, String email) {
    }
}
