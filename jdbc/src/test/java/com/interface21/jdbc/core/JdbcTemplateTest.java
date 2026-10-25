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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SELECT_SQL = "select name from users";
    private static final String UPDATE_SQL = "update users set name = ?, email = ? where id = ?";
    private static final RowMapper<String> NAME_ROW_MAPPER = resultSet -> resultSet.getString("name");

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
    void updateBindsParametersAndReturnsAffectedRowCount() throws SQLException {
        when(statement.executeUpdate()).thenReturn(2);

        final var count = jdbcTemplate.update(UPDATE_SQL, "gugu", null, 1L);

        assertThat(count).isEqualTo(2);
        verify(connection).prepareStatement(UPDATE_SQL);
        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, null);
        verify(statement).setObject(3, 1L);
        verifyStatementAndConnectionClosed();
    }

    @Test
    void queryBindsParametersAndMapsEveryRow() throws SQLException {
        final var sql = SELECT_SQL + " where id > ?";
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("gugu", "eian");

        final var names = jdbcTemplate.query(sql, NAME_ROW_MAPPER, 3L);

        assertThat(names).containsExactly("gugu", "eian");
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, 3L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryReturnsEmptyListWithoutMappingWhenNoRowsExist() throws SQLException {
        final var names = jdbcTemplate.query(SELECT_SQL, NAME_ROW_MAPPER);

        assertThat(names).isEmpty();
        verify(resultSet, never()).getString(anyString());
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsTheMappedRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("gugu");

        final var name = jdbcTemplate.queryForObject(SELECT_SQL, NAME_ROW_MAPPER);

        assertThat(name).isEqualTo("gugu");
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsNullWhenNoRowsExist() throws SQLException {
        final var name = jdbcTemplate.queryForObject(SELECT_SQL, NAME_ROW_MAPPER);

        assertThat(name).isNull();
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("gugu", "eian");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SELECT_SQL, NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class);

        verifyQueryResourcesClosed();
    }

    @Test
    void translatesConnectionFailureAndPreservesCause() throws SQLException {
        final var failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "gugu", null, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection, never()).prepareStatement(anyString());
    }

    @Test
    void closesConnectionWhenStatementCreationFails() throws SQLException {
        final var failure = new SQLException("invalid SQL");
        when(connection.prepareStatement(SELECT_SQL)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).close();
    }

    @Test
    void closesStatementAndConnectionWhenParameterBindingFails() throws SQLException {
        final var failure = new SQLException("invalid parameter");
        doThrow(failure).when(statement).setObject(1, "gugu");

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "gugu", null, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement, never()).executeUpdate();
        verifyStatementAndConnectionClosed();
    }

    @Test
    void closesStatementAndConnectionWhenUpdateFails() throws SQLException {
        final var failure = new SQLException("constraint violation");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "gugu", null, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyStatementAndConnectionClosed();
    }

    @Test
    void closesStatementAndConnectionWhenQueryFails() throws SQLException {
        final var failure = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyStatementAndConnectionClosed();
    }

    @Test
    void closesAllResourcesWhenReadingNextRowFails() throws SQLException {
        final var failure = new SQLException("result unavailable");
        when(resultSet.next()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    void closesAllResourcesWhenRowMappingThrowsSQLException() throws SQLException {
        final var failure = new SQLException("column unavailable");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    void closesAllResourcesAndPreservesUncheckedMappingFailure() throws SQLException {
        final var failure = new IllegalArgumentException("invalid domain value");
        when(resultSet.next()).thenReturn(true, false);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, row -> {
            throw failure;
        })).isSameAs(failure);

        verifyQueryResourcesClosed();
    }

    private void verifyStatementAndConnectionClosed() throws SQLException {
        verify(statement).close();
        verify(connection).close();
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verifyStatementAndConnectionClosed();
    }
}
