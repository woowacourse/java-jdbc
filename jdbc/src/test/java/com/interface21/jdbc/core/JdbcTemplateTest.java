package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

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
        when(statement.executeUpdate()).thenReturn(1);
        assertThat(template.update("update test set value = ?", "value")).isEqualTo(1);
        verify(statement).setObject(1, "value");
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMapsEveryRowAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        assertThat(template.query("select value from test", (rs, row) -> row)).containsExactly(0, 1);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void emptyQueryReturnsNull() {
        assertThat(template.<Integer>queryForObject("select value from test", (rs, row) -> row)).isNull();
    }

    @Test
    void multipleRowsAreRejected() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        assertThatThrownBy(() -> template.queryForObject("select value from test", (rs, row) -> row))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mappingFailurePreservesCauseAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        var cause = new SQLException("mapping failed");
        assertThatThrownBy(() -> template.query("select value from test", (rs, row) -> { throw cause; }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
    @Test
    void updateAcceptsCustomParameterSetter() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);
        assertThat(template.update("update test set value = ?", ps -> ps.setString(1, "value")))
                .isEqualTo(1);
        verify(statement).setString(1, "value");
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryAcceptsCustomParameterSetter() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        assertThat(template.query("select value from test where id = ?", ps -> ps.setLong(1, 7L),
                (rs, row) -> row)).containsExactly(0);
        verify(statement).setLong(1, 7L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void bindingFailureClosesResourcesAndPreservesCause() throws SQLException {
        var cause = new SQLException("binding failed");
        assertThatThrownBy(() -> template.update("update test set value = ?", ps -> { throw cause; }))
                .isInstanceOf(DataAccessException.class).hasCause(cause);
        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void executionFailureClosesResourcesAndPreservesCause() throws SQLException {
        var cause = new SQLException("execution failed");
        when(statement.executeUpdate()).thenThrow(cause);
        assertThatThrownBy(() -> template.update("delete from test"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void connectionFailureIsTranslated() throws SQLException {
        var cause = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(cause);
        assertThatThrownBy(() -> template.update("delete from test"))
                .isInstanceOf(DataAccessException.class).hasCause(cause);
    }

    @Test
    void callerOwnsExplicitConnection() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);
        assertThat(template.update(connection, "delete from test")).isEqualTo(1);
        verify(statement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

}
