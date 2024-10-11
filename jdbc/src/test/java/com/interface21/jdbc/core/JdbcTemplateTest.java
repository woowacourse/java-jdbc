package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private RowMapper<TestUser> rowMapper;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        rowMapper = mock(RowMapper.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @DisplayName("데이터를 성공적으로 추가한다.")
    @Test
    void update() throws SQLException {
        // given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        TestUser user = new TestUser(1L, "account", "password", "email");

        // when
        jdbcTemplate.update(sql, () -> {
            final var parameters = new Parameters();
            parameters.add(1, user.getAccount());
            parameters.add(2, user.getPassword());
            parameters.add(3, user.getEmail());

            return parameters;
        });

        // then
        when(resultSet.next()).thenReturn(true);
        when(rowMapper.mapRow(any())).thenReturn(user);
        TestUser expectedUser = jdbcTemplate.queryForObject("select * from users where id = ?", () -> {
            final var parameters = new Parameters();
            parameters.add(1, user.getId());
            return parameters;
        }, rowMapper);

        assertThat(user).isEqualTo(expectedUser);
    }

    @DisplayName("단일 결과를 성공적으로 조회한다.")
    @Test
    void queryForObject() throws SQLException {
        // given
        TestUser user = new TestUser(1L, "account", "password", "email");
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)", () -> {
                    final var parameters = new Parameters();
                    parameters.add(1, user.getAccount());
                    parameters.add(2, user.getPassword());
                    parameters.add(3, user.getEmail());

                    return parameters;
                });
        when(resultSet.next()).thenReturn(true);
        when(rowMapper.mapRow(any())).thenReturn(user);

        // when
        String sql = "select * from users where id = ?";
        TestUser resultUser = jdbcTemplate.queryForObject(sql, () -> {
            final var parameters = new Parameters();
            parameters.add(1, user.getId());
            return parameters;
        }, rowMapper);

        // then
        assertThat(resultUser).isNotNull();
        assertThat(resultUser).isEqualTo(user);

        verify(preparedStatement).setObject(1, user.getId());
        verify(preparedStatement).executeQuery();
        verify(resultSet).next();
    }

    @DisplayName("다수의 결과를 성공적으로 조회한다.")
    @Test
    void query() throws SQLException {
        // given
        String sql = "select * from users";
        TestUser user1 = new TestUser(1L, "account1", "password1", "email1");
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)", () -> {
                    final var parameters = new Parameters();
                    parameters.add(1, user1.getAccount());
                    parameters.add(2, user1.getPassword());
                    parameters.add(3, user1.getEmail());

                    return parameters;
                });
        TestUser user2 = new TestUser(2L, "account2", "password2", "email2");
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)", () -> {
                    final var parameters = new Parameters();
                    parameters.add(1, user2.getAccount());
                    parameters.add(2, user2.getPassword());
                    parameters.add(3, user2.getEmail());

                    return parameters;
                });
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(any())).thenReturn(user1, user2);

        // when
        List<TestUser> users = jdbcTemplate.query(sql, new Parameters(), rowMapper);

        // then
        assertThat(users).hasSize(2);
        assertThat(users).containsExactly(user1, user2);

        verify(preparedStatement).executeQuery();
        verify(resultSet, times(3)).next();
    }
}
