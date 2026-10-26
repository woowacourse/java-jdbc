package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void updateBindsParametersAndClosesResources() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update("update users set email = ? where id = ?", null, 3L)).isEqualTo(1);

        verify(statement).setObject(1, null);
        verify(statement).setObject(2, 3L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMapsEveryRowAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("first", "second");

        assertThat(jdbcTemplate.query("select account from users", rs -> rs.getString("account")))
                .containsExactly("first", "second");

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void emptyResults() {
        assertThat(jdbcTemplate.query("select account from users", rs -> rs.getString("account"))).isEmpty();
        assertThat(jdbcTemplate.<String>queryForObject("select account from users", rs -> rs.getString("account"))).isNull();
    }

    @Test
    void singleResult() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.<String>queryForObject("select account from users where id = ?",
                rs -> rs.getString("account"), 1L)).isEqualTo("gugu");

        verify(statement).setObject(1, 1L);
    }

    @Test
    void multipleResultsAreRejected() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users", rs -> "user"))
                .isInstanceOf(IllegalStateException.class);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateFailurePreservesCauseAndClosesResources() throws SQLException {
        final var failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql"))
                .isInstanceOf(RuntimeException.class)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void mappingFailurePreservesCauseAndClosesResources() throws SQLException {
        final var failure = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rs -> {
            throw failure;
        })).isInstanceOf(RuntimeException.class).hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void preparationFailureClosesConnection() throws SQLException {
        final var failure = new SQLException("prepare failed");
        when(connection.prepareStatement(anyString())).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql")).hasCause(failure);
        verify(connection).close();
    }
}
