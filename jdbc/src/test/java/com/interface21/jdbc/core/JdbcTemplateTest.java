package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource = mock(DataSource.class);
    private Connection connection = mock(Connection.class);
    private PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private ResultSet resultSet = mock(ResultSet.class);

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void update() throws Exception {
        // given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // when
        jdbcTemplate.update(sql, "mia", "password", "mia@gmail.com");

        // then
        verify(preparedStatement).setObject(1, "mia");
        verify(preparedStatement).setObject(2, "password");
        verify(preparedStatement).setObject(3, "mia@gmail.com");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void query() throws Exception {
        // given
        String sql = "select id, account, password, email from users where id = ?";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getLong("id")).thenReturn(1l);
        when(resultSet.getString("account")).thenReturn("mia");

        ResultSetMapper<TestUser> resultSetMapper = rs -> {
            if (rs.next()) {
                return new TestUser(rs.getLong("id"), rs.getString("account"));
            }
            return null;
        };

        // when
        TestUser queriedTestUser = jdbcTemplate.query(sql, resultSetMapper, 1l);

        // then
        assertAll(() -> {
            assertThat(queriedTestUser.id).isEqualTo(1);
            assertThat(queriedTestUser.account).isEqualTo("mia");
        });
    }

    private static class TestUser {
        private Long id;
        private String account;

        public TestUser(Long id, String account) {
            this.id = id;
            this.account = account;
        }
    }
}
