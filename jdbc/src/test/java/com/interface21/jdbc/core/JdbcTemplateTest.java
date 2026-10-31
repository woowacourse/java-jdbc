package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
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
                .isInstanceOf(DataAccessException.class);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateFailurePreservesCauseAndClosesResources() throws SQLException {
        final var failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql"))
                .isInstanceOf(DataAccessException.class)
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
        })).isInstanceOf(DataAccessException.class).hasCause(failure);
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

    @Test
    void customSetterForUpdate() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update("update users set email = ? where id = ?", ps -> {
            ps.setString(1, "user@example.com");
            ps.setLong(2, 1L);
        })).isEqualTo(1);

        verify(statement).setString(1, "user@example.com");
        verify(statement).setLong(2, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void customSetterForQuery() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var account = jdbcTemplate.queryForObject("select account from users where id = ?",
                ps -> ps.setLong(1, 1L), rs -> rs.getString("account"));

        assertThat(account).isEqualTo("gugu");
        verify(statement).setLong(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void setterFailureClosesResourcesWithoutExecuting() throws SQLException {
        final var failure = new SQLException("binding failed");

        assertThatThrownBy(() -> jdbcTemplate.update("update users set email = ?", ps -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryFailureClosesResources() throws SQLException {
        final var failure = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query("invalid sql", rs -> "user"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void connectionFailureIsTranslated() throws SQLException {
        final var failure = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("update users set email = ?", "email"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("update users set email = ?")
                .hasCause(failure);
        verifyNoInteractions(connection);
    }

    @Test
    void uncheckedMapperFailureIsPreservedAndClosesResources() throws SQLException {
        final var failure = new IllegalArgumentException("invalid result");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rs -> {
            throw failure;
        })).isSameAs(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void closeFailureIsSuppressedByOriginalFailure() throws SQLException {
        final var failure = new SQLException("update failed");
        final var closeFailure = new SQLException("close failed");
        when(statement.executeUpdate()).thenThrow(failure);
        doThrow(closeFailure).when(statement).close();

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        assertThat(failure.getSuppressed()).containsExactly(closeFailure);
        verify(connection).close();
    }
}
