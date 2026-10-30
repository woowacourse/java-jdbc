package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        final String sql = "update users set password = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(1);

        final int updated = jdbcTemplate.update(sql, "newPassword", 1L);

        assertThat(updated).isEqualTo(1);
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, "newPassword");
        verify(statement).setObject(2, 1L);
    }

    @Test
    void query() throws SQLException {
        final String sql = "select account from users";
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "dongkey");

        final List<String> accounts = jdbcTemplate.query(sql, row -> row.getString("account"));

        assertThat(accounts).containsExactly("gugu", "dongkey");
        verify(connection).prepareStatement(sql);
    }

    @Test
    void queryForObject() throws SQLException {
        final String sql = "select account from users where id = ?";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final Optional<String> account = jdbcTemplate.queryForObject(sql, row -> row.getString("account"), 1L);

        assertThat(account).contains("gugu");
        verify(statement).setObject(1, 1L);
    }

    @Test
    void queryForObjectWithoutResults() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final Optional<String> account = jdbcTemplate.queryForObject("select account from users", row -> row.getString("account"));

        assertThat(account).isEmpty();
    }

    @Test
    void queryForObjectWithMultipleResults() throws SQLException {
        final String sql = "select account from users";
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void sqlException() throws SQLException {
        final SQLException exception = new SQLException("쿼리 실행 실패");
        when(statement.executeUpdate()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("update users set password = ?", "newPassword"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);
        verify(statement).close();
        verify(connection).close();
    }
}
