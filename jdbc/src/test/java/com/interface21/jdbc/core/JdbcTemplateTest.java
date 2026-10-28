package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private RowMapper<String> rowMapper;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        rowMapper = mock();
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("sql")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void updateBindsDifferentTypesAndClosesResources() throws SQLException {
        jdbcTemplate.update("sql", new Object[]{"title", 1L, null});

        verify(statement).setObject(1, "title");
        verify(statement).setObject(2, 1L);
        verify(statement).setObject(3, null);
        verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMapsRowsAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn("first", "second");

        assertThat(jdbcTemplate.query("sql", new Object[]{7L}, rowMapper))
                .containsExactly("first", "second");
        verify(statement).setObject(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectRejectsEmptyResult() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("sql", new Object[0], rowMapper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expected 1 row but found 0");
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsSingleRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet)).thenReturn("only row");

        assertThat(jdbcTemplate.queryForObject("sql", new Object[0], rowMapper)).isEqualTo("only row");
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("sql", new Object[0], rowMapper))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expected 1 row but found 2");
        verifyQueryResourcesClosed();
    }

    @Test
    void updateFailurePreservesCauseAndClosesResources() throws SQLException {
        final var failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("sql", new Object[0])).hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void mappingFailurePreservesCauseAndClosesResources() throws SQLException {
        final var failure = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);
        when(rowMapper.mapRow(resultSet)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query("sql", new Object[0], rowMapper)).hasCause(failure);
        verifyQueryResourcesClosed();
    }

    @Test
    void statementCreationFailureClosesConnection() throws SQLException {
        final var failure = new SQLException("prepare failed");
        when(connection.prepareStatement("sql")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query("sql", new Object[0], rowMapper)).hasCause(failure);
        verify(connection).close();
    }

    @Test
    void closeFailureStillClosesRemainingResources() throws SQLException {
        doThrow(new SQLException("close failed")).when(resultSet).close();

        assertThat(jdbcTemplate.query("sql", new Object[0], rowMapper)).isEmpty();
        verifyQueryResourcesClosed();
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
