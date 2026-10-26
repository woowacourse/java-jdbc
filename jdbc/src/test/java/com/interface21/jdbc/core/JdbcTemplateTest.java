package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate template = new JdbcTemplate(dataSource);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void updateBindsArgumentsAndClosesResources() throws SQLException {
        when(statement.executeUpdate()).thenReturn(2);
        assertThat(template.update("update example set name = ? where id = ?", null, 7L)).isEqualTo(2);
        verify(statement).setObject(1, null);
        verify(statement).setObject(2, 7L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMapsAllRowsAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("first", "second");
        assertThat(template.query("select name from example", (rs, row) -> row + rs.getString("name")))
                .containsExactly("0first", "1second");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void emptyResults() {
        assertThat(template.query("select name from example", (rs, row) -> "unused")).isEmpty();
        assertThat(template.<String>queryForObject("select name from example", (rs, row) -> "unused")).isNull();
    }

    @Test
    void singleResult() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        assertThat(template.<String>queryForObject("select name from example", (rs, row) -> "value"))
                .isEqualTo("value");
    }

    @Test
    void multipleResultsAreRejected() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        assertThatThrownBy(() -> template.queryForObject("select name from example", (rs, row) -> row))
                .isInstanceOf(DataAccessException.class);
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    void mappingFailureClosesAllResourcesAndPreservesCause() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        final var failure = new SQLException("mapping failed");
        assertThatThrownBy(() -> template.query("select name from example", (rs, row) -> { throw failure; }))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void bindingFailureClosesResources() throws SQLException {
        final var failure = new SQLException("binding failed");
        doThrow(failure).when(statement).setObject(1, "value");
        assertThatThrownBy(() -> template.update("insert into example values (?)", "value"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void statementCreationFailureClosesConnection() throws SQLException {
        final var failure = new SQLException("invalid SQL");
        when(connection.prepareStatement(anyString())).thenThrow(failure);
        assertThatThrownBy(() -> template.update("invalid"))
                .isInstanceOf(DataAccessException.class).hasCause(failure);
        verify(connection).close();
    }
}
