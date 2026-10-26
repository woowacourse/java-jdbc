package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);

    @Test
    void queryForObjectReturnsNullWhenNoRowsAreFound() throws SQLException {
        final var jdbcTemplate = jdbcTemplate();
        when(resultSet.next()).thenReturn(false);

        final var actual = jdbcTemplate.queryForObject("select value", resultSet -> resultSet.getString("value"));

        assertThat(actual).isNull();
    }

    @Test
    void queryForObjectReturnsMappedValueWhenOneRowIsFound() throws SQLException {
        final var jdbcTemplate = jdbcTemplate();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("value")).thenReturn("one");

        final var actual = jdbcTemplate.queryForObject("select value", resultSet -> resultSet.getString("value"));

        assertThat(actual).isEqualTo("one");
    }

    @Test
    void queryForObjectThrowsWhenMultipleRowsAreFound() throws SQLException {
        final var jdbcTemplate = jdbcTemplate();
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("value")).thenReturn("one", "two");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select value", resultSet -> resultSet.getString("value")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("2");
    }

    @Test
    void updateClosesResourcesAndPreservesCauseWhenSetterThrowsSQLException() throws SQLException {
        final var jdbcTemplate = jdbcTemplate();
        final var cause = new SQLException("Could not bind query parameter");

        assertThatThrownBy(() -> jdbcTemplate.update("update users set name = ?", setter -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void queryClosesResourcesAndPreservesCauseWhenRowMapperThrowsSQLException() throws SQLException {
        final var jdbcTemplate = jdbcTemplate();
        when(resultSet.next()).thenReturn(true);
        final var cause = new SQLException("Could not map row");

        assertThatThrownBy(() -> jdbcTemplate.query("select value", resultSet -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    private JdbcTemplate jdbcTemplate() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        return new JdbcTemplate(dataSource);
    }
}
