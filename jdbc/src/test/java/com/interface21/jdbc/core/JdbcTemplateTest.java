package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

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
    void updateKeepsExternalConnectionOpen() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final var actual = jdbcTemplate.update(connection, "update users set password = ?", "newPassword");

        assertThat(actual).isEqualTo(1);
        verify(statement).setObject(1, "newPassword");
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void updateFailureKeepsExternalConnectionOpen() throws SQLException {
        final var failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, "invalid sql"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void queryForObjectKeepsExternalConnectionOpen() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("password")).thenReturn("password");

        final var actual = jdbcTemplate.queryForObject(connection,
                "select password from users where id = ?", rs -> rs.getString("password"), 1L);

        assertThat(actual).isEqualTo("password");
        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void mappingFailureKeepsExternalConnectionOpen() throws SQLException {
        final var failure = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(connection, "select password from users", rs -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void updateClosesOwnedConnection() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update("update users set password = ?", "newPassword")).isEqualTo(1);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateFailureClosesOwnedConnection() throws SQLException {
        final var failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectClosesOwnedConnection() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("password")).thenReturn("password");

        final var actual = jdbcTemplate.queryForObject("select password from users", rs -> rs.getString("password"));

        assertThat(actual).isEqualTo("password");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
