package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);

        resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("query() 메소드를 통해 DB에서 값을 조회할 수 있다.")
    void query() throws SQLException {
        final String query = "select * from users";
        final RowMapper<User> rowMapper = getUserRowMapper();

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);
        final List<User> users = jdbcTemplate.query(query, rowMapper);

        assertAll(
                () -> assertThat(users).hasSize(1),
                () -> assertThat(users.getFirst().getAccount()).isEqualTo("gugu")
        );
    }

    @Test
    @DisplayName("모든 작업이 끝난 후에는 DataSource, Connection, ResultSet이 종료되어야 한다.")
    void verifyResourceClosed() throws SQLException {
        final String query = "select * from users";
        final RowMapper<User> rowMapper = getUserRowMapper();

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);
        jdbcTemplate.query(query, rowMapper);

        verify(preparedStatement, atLeastOnce()).close();
        verify(connection, atLeastOnce()).close();
        verify(resultSet, atLeastOnce()).close();
    }

    @Test
    @DisplayName("queryForObject() 메소드를 통해 DB에서 값을 조회할 수 있다.")
    void queryForObject() throws SQLException {
        final String query = "select id, account, password, email from users where id = ?";
        final RowMapper<User> rowMapper = getUserRowMapper();

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);
        final User user = jdbcTemplate.queryForObject(query, rowMapper, 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    @DisplayName("queryForObject() 메소드는 유일한 값만 조회할 수 있다.")
    void validateUniqueOfQueryForObjectResult() throws SQLException {
        final String query = "select id, account, password, email from users where id = ?";
        final RowMapper<User> rowMapper = getUserRowMapper();

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(query, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Incorrect result size: expected 1, actual 2");
    }

    @Test
    @DisplayName("queryForObject() 메소드의 결과는 항상 존재해야 한다.")
    void validateExistOfQueryForObjectResult() throws SQLException {
        final String query = "select id, account, password, email from users where id = ?";
        final RowMapper<User> rowMapper = getUserRowMapper();

        when(resultSet.next())
                .thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(query, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Incorrect result size: expected 1, actual 0");
    }

    private RowMapper<User> getUserRowMapper() {
        return (rs, rowNum) -> new User(
                1L,
                "gugu",
                "password",
                "gugu@test.com");
    }
}
