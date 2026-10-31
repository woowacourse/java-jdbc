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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "select value from test";
    private static final RowMapper<String> ROW_MAPPER = resultSet -> resultSet.getString("value");

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void updateBindsParametersInOrderAndReturnsAffectedRows() throws Exception {
        when(statement.executeUpdate()).thenReturn(3);

        final var affectedRows = jdbcTemplate.update(SQL, "a", 10);

        assertThat(affectedRows).isEqualTo(3);
        final var order = inOrder(statement);
        order.verify(statement).setObject(1, "a");
        order.verify(statement).setObject(2, 10);
        order.verify(statement).executeUpdate();
    }

    @Test
    void updateWithoutArgumentsBindsNothing() throws Exception {
        jdbcTemplate.update(SQL);

        verify(statement, never()).setObject(anyInt(), any());
        verify(statement).executeUpdate();
    }

    @Test
    void updateClosesStatementAndConnection() throws Exception {
        jdbcTemplate.update(SQL);

        verifyClosedInOrder(statement, connection);
    }

    @Test
    void queryForObjectReturnsMappedRow() throws Exception {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("value")).thenReturn("first");

        final var actual = jdbcTemplate.queryForObject(SQL, ROW_MAPPER, "a");

        assertThat(actual).isEqualTo("first");
        verify(statement).setObject(1, "a");
        verifyClosedInOrder(resultSet, statement, connection);
    }

    @Test
    void queryForObjectReturnsNullWhenNoRows() throws Exception {
        when(resultSet.next()).thenReturn(false);

        final var actual = jdbcTemplate.queryForObject(SQL, ROW_MAPPER);

        assertThat(actual).isNull();
        verifyClosedInOrder(resultSet, statement, connection);
    }

    @Test
    void queryForObjectReturnsFirstRowWhenMultipleRows() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("value")).thenReturn("first", "second");

        final var actual = jdbcTemplate.queryForObject(SQL, ROW_MAPPER);

        assertThat(actual).isEqualTo("first");
    }

    @Test
    void queryForObjectTranslatesSqlException() throws Exception {
        final var failure = new SQLException("failure");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(SQL)
                .hasCauseReference(failure);
        verifyClosedInOrder(statement, connection);
    }

    @Test
    void queryMapsAllRowsInOrder() throws Exception {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("value")).thenReturn("first", "second");

        final var actual = jdbcTemplate.query(SQL, ROW_MAPPER, "a");

        assertThat(actual).containsExactly("first", "second");
        verify(statement).setObject(1, "a");
        verifyClosedInOrder(resultSet, statement, connection);
    }

    @Test
    void queryReturnsEmptyListWhenNoRows() throws Exception {
        when(resultSet.next()).thenReturn(false);

        final var actual = jdbcTemplate.query(SQL, ROW_MAPPER);

        assertThat(actual).isEmpty();
        verifyClosedInOrder(resultSet, statement, connection);
    }

    @Test
    void translatesExceptionWhenGettingConnectionFails() throws Exception {
        final var failure = new SQLException("failure");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(SQL)
                .hasCauseReference(failure);
        verify(connection, never()).prepareStatement(anyString());
    }

    @Test
    void closesConnectionWhenPreparingStatementFails() throws Exception {
        final var failure = new SQLException("failure");
        when(connection.prepareStatement(SQL)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCauseReference(failure);
        verify(connection).close();
    }

    @Test
    void doesNotExecuteAndClosesResourcesWhenBindingFails() throws Exception {
        final var failure = new SQLException("failure");
        doThrow(failure).when(statement).setObject(1, "a");

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, "a"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseReference(failure);
        verify(statement, never()).executeUpdate();
        verifyClosedInOrder(statement, connection);
    }

    @Test
    void closesResourcesWhenUpdateFails() throws Exception {
        final var failure = new SQLException("failure");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(SQL)
                .hasCauseReference(failure);
        verifyClosedInOrder(statement, connection);
    }

    @Test
    void closesResourcesWhenQueryFails() throws Exception {
        final var failure = new SQLException("failure");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(SQL)
                .hasCauseReference(failure);
        verifyClosedInOrder(statement, connection);
    }

    @Test
    void translatesSqlExceptionFromRowMapperAndClosesResources() throws Exception {
        final var failure = new SQLException("failure");
        final RowMapper<String> failingRowMapper = rs -> {
            throw failure;
        };
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, failingRowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(SQL)
                .hasCauseReference(failure);
        verifyClosedInOrder(resultSet, statement, connection);
    }

    @Test
    void propagatesRuntimeExceptionFromRowMapperAndClosesResources() throws Exception {
        final var failure = new IllegalStateException("failure");
        final RowMapper<String> failingRowMapper = rs -> {
            throw failure;
        };
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, failingRowMapper))
                .isSameAs(failure);
        verifyClosedInOrder(resultSet, statement, connection);
    }

    private void verifyClosedInOrder(final AutoCloseable... resources) throws Exception {
        final var order = inOrder((Object[]) resources);
        for (final var resource : resources) {
            order.verify(resource).close();
        }
    }
}
