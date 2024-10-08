package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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

import com.interface21.dao.DataAccessException;
import com.techcourse.domain.User;

class JdbcTemplateTest {

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private JdbcTemplate jdbcTemplate;

    private DataSource dataSource;

    private Connection connection;

    private PreparedStatement statement;

    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("queryForObject 메서드에서 sql 실행 결과가 없을 경우 예외가 발생한다.")
    void query_for_object_throw_exception_no_result() throws SQLException {
        // given
        final String sql = "SELECT * FROM users WHERE id = ?";
        final long id = 1L;
        when(resultSet.next()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, ROW_MAPPER, id))
                .isExactlyInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("queryForObject 메서드에서 sql 실행 결과가 2개 이상일 경우 예외가 발생한다.")
    void query_for_object_throw_exception_over_two_result() {
        // given
        insert("lemon", "lemon11", "lemon@wooteco.com");
        insert("robin", "robin1234", "robin@wooteco.com");
        final String sql = "SELECT * FROM users WHERE account = ?";
        final String account = "robin";

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, ROW_MAPPER, account))
                .isExactlyInstanceOf(DataAccessException.class);
    }

    private void insert(final String account, final String password, final String email) {
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, account, password, email);
    }

    @Test
    @DisplayName("데이터를 추가하는 sql문을 실행한다.")
    void update() throws SQLException {
        // given
        String sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        String account = "lemon";
        String password = "lemon12";
        String email = "lemon@wooteco.com";

        // when
        jdbcTemplate.update(sql, account, password, email);

        // then
        verify(statement).setObject(1, account);
        verify(statement).setObject(2, password);
        verify(statement).setObject(3, email);
    }
}
