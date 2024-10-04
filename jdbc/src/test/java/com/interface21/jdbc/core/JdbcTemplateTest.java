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
import org.junit.jupiter.api.Test;

import com.techcourse.domain.User;

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

    @Test
    @DisplayName("INSERT 문, 데이터 추가 성공")
    void insert() throws SQLException {
        var sql = "insert into users (account, password, email) values (?, ?, ?)";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        var expected = 1;
        when(statement.executeUpdate()).thenReturn(expected);
        var account = "kyummi";
        var password = "password";
        var email = "kyum@naver.com";

        var actual = jdbcTemplate.update(sql, account, password, email);

        assertAll(
                () -> assertThat(actual).isEqualTo(expected),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(statement).setObject(1, account),
                () -> verify(statement).setObject(2, password),
                () -> verify(statement).setObject(3, email),
                () -> verify(statement).executeUpdate()
        );
    }

    @Test
    @DisplayName("UPDATE 문, 데이터 수정 성공")
    void update() throws SQLException {
        var sql = "update users  set account = ?, password = ?, email = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        var expected = 1;
        when(statement.executeUpdate()).thenReturn(expected);
        var account = "kyummi";
        var password = "password";
        var email = "kyum@naver.com";

        var actual = jdbcTemplate.update(sql, account, password, email);

        assertAll(
                () -> assertThat(actual).isEqualTo(expected),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(statement).setObject(1, account),
                () -> verify(statement).setObject(2, password),
                () -> verify(statement).setObject(3, email),
                () -> verify(statement).executeUpdate()
        );
    }

    @Test
    @DisplayName("SELECT 문, 데이터 검색 성공 (2개)")
    void query() throws SQLException {
        // given
        var sql = "select id, account, password, email from users";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        var user1 = new User(1L, "kyummi", "password", "kyum@naver.com");
        var user2 = new User(2L, "kiki", "password", "kikinaver.com");
        when(resultSet.getLong(1)).thenReturn(user1.getId()).thenReturn(user2.getId());
        when(resultSet.getString(2)).thenReturn(user1.getAccount()).thenReturn(user2.getAccount());
        when(resultSet.getString(3)).thenReturn(user1.getPassword()).thenReturn(user2.getPassword());
        when(resultSet.getString(4)).thenReturn(user1.getEmail()).thenReturn(user2.getEmail());

        when(statement.executeQuery()).thenReturn(resultSet);

        final RowMapper<User> rowMapper = (rs, size) ->
                new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));

        // when
        var actual = jdbcTemplate.query(sql, rowMapper);

        // then
        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual).extracting("id")
                        .containsExactly(user1.getId(), user2.getId()),
                () -> assertThat(actual).extracting("account")
                        .containsExactly(user1.getAccount(), user2.getAccount()),
                () -> assertThat(actual).extracting("password")
                        .containsExactly(user1.getPassword(), user2.getPassword()),
                () -> assertThat(actual).extracting("email")
                        .containsExactly(user1.getEmail(), user2.getEmail()),
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

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);

        var user1 = new User(1L, "kyummi", "password", "kyum@naver.com");
        when(resultSet.getLong(1)).thenReturn(user1.getId());
        when(resultSet.getString(2)).thenReturn(user1.getAccount());
        when(resultSet.getString(3)).thenReturn(user1.getPassword());
        when(resultSet.getString(4)).thenReturn(user1.getEmail());

        when(statement.executeQuery()).thenReturn(resultSet);

        final RowMapper<User> rowMapper = (rs, size) ->
                new User(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
        var id = 1L;

        // when
        var actual = jdbcTemplate.queryObject(sql, rowMapper, id);

        // then
        assertAll(
                () -> assertThat(actual).isEqualTo(user1),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(statement).setObject(1, id),
                () -> verify(statement).executeQuery()
        );
    }
}
