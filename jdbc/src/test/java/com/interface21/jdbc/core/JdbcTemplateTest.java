package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement prepareStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
        connection = mock(Connection.class);
        prepareStatement = mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(prepareStatement);
        when(prepareStatement.executeQuery()).thenReturn(resultSet);
    }

    @DisplayName("jdbc를 이용해 INSERT문을 실행한다. ")
    @Test
    void update_insert() {
        // given
        final String sql = "INSERT INTO users (account, password, email) VALUES (?,?,?)";
        String account = "gugu";
        String password = "password";
        String email = "hkkang@woowahan.com";

        // when
        jdbcTemplate.update(sql, account, password, email);

        // then
        assertAll(
                () -> verify(prepareStatement).setObject(1, account),
                () -> verify(prepareStatement).setObject(2, password),
                () -> verify(prepareStatement).setObject(3, email),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(prepareStatement).executeUpdate(),
                () -> verify(connection, times(1)).close()
        );
    }

    @DisplayName("jdbc 이용해 UPDATE문을 실행한다. ")
    @Test
    void update_update() {
        // given
        final String sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        String account = "gugu";
        String password = "password";
        String email = "hkkang@woowahan.com";

        // when
        jdbcTemplate.update(sql, account, password, email);

        // then
        assertAll(
                () -> verify(prepareStatement).setObject(1, account),
                () -> verify(prepareStatement).setObject(2, password),
                () -> verify(prepareStatement).setObject(3, email),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(prepareStatement).executeUpdate(),
                () -> verify(connection, times(1)).close()
        );
    }

    @DisplayName("jdbc를 이용해 여러개의 데이터를 SELECT한다. ")
    @Test
    void query() throws SQLException {
        // given
        String sql = "SELECT id, account, password, email FROM users";
        User gugu = new User(1L, "gugu", "password", "email");
        User kyum = new User(2L, "kyum", "password", "emil");
        User rush = new User(3L, "rush", "password", "emil");

        when(resultSet.next()).thenReturn(true, true, true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L, 3L);
        when(resultSet.getString("account")).thenReturn(gugu.account, kyum.account, rush.account);
        when(resultSet.getString("password")).thenReturn(gugu.password, kyum.password, rush.password);
        when(resultSet.getString("email")).thenReturn(gugu.email, kyum.email, rush.email);

        // when
        List<User> users = jdbcTemplate.query(sql, rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        ));

        // then
        assertAll(
                () -> assertThat(users).contains(gugu, kyum, rush),
                () -> verify(prepareStatement, times(1)).executeQuery(),
                () -> verify(resultSet, times(5)).next(),
                () -> verify(connection, times(1)).close()
        );
    }

    @DisplayName("jdbc를 이용해 하나의 데이터를 SELECT한다.")
    @Test
    void queryForObject() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users where id = ?";
        User gugu = new User(1L, "gugu", "password", "email");

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn(gugu.account);
        when(resultSet.getString("password")).thenReturn(gugu.password);
        when(resultSet.getString("email")).thenReturn(gugu.email);

        // when
        User findUser = jdbcTemplate.queryForObject(sql, rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        ), 1L);

        // then
        assertAll(
                () -> assertThat(findUser).isEqualTo(gugu),
                () -> verify(prepareStatement, times(1)).executeQuery(),
                () -> verify(resultSet, times(2)).next(),
                () -> verify(connection, times(1)).close()
        );
    }

    record User(Long id, String account, String password, String email) {

    }
}

