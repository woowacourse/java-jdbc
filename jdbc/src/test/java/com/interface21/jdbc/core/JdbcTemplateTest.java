package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String UPDATE_SQL = "update users set name = ?, age = ? where id = ?";
    private static final String QUERY_SQL = "select name from users where age >= ? and active = ?";

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;
    private RowMapper<String> rowMapper;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        rowMapper = mock(RowMapper.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(UPDATE_SQL)).thenReturn(statement);
        when(connection.prepareStatement(QUERY_SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void updateBindsArgumentsInOrderStartingAtOneAndReturnsAffectedRows() throws SQLException {
        when(statement.executeUpdate()).thenReturn(3);

        int affectedRows = jdbcTemplate.update(UPDATE_SQL, "alice", null, 7L);

        assertThat(affectedRows).isEqualTo(3);
        InOrder order = inOrder(dataSource, connection, statement);
        order.verify(dataSource).getConnection();
        order.verify(connection).prepareStatement(UPDATE_SQL);
        order.verify(statement).setObject(1, "alice");
        order.verify(statement).setObject(2, null);
        order.verify(statement).setObject(3, 7L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
        verifyNoMoreInteractions(dataSource, connection, statement);
    }

    @Test
    void updateWithoutArgumentsReturnsZeroWhenNoRowsAreAffected() throws SQLException {
        when(statement.executeUpdate()).thenReturn(0);

        assertThat(jdbcTemplate.update(UPDATE_SQL)).isZero();

        verify(statement).executeUpdate();
        verifyStatementAndConnectionClosed();
        verifyNoMoreInteractions(statement);
    }

    @Test
    void queryBindsArgumentsAndMapsEveryRowWithZeroBasedRowNumbers() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("alice", "bob");

        List<String> rows = jdbcTemplate.query(QUERY_SQL,
                (rs, rowNum) -> rowNum + ":" + rs.getString("name"), 20, true);

        assertThat(rows).containsExactly("0:alice", "1:bob");
        InOrder order = inOrder(connection, statement, resultSet);
        order.verify(connection).prepareStatement(QUERY_SQL);
        order.verify(statement).setObject(1, 20);
        order.verify(statement).setObject(2, true);
        order.verify(statement).executeQuery();
        order.verify(resultSet).next();
        order.verify(resultSet).getString("name");
        order.verify(resultSet).next();
        order.verify(resultSet).getString("name");
        order.verify(resultSet).next();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
        verifyNoMoreInteractions(connection, statement, resultSet);
    }

    @Test
    void queryWithNoRowsReturnsAnEmptyListWithoutCallingMapper() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThat(jdbcTemplate.query(QUERY_SQL, rowMapper)).isEmpty();

        verifyNoInteractions(rowMapper);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectWithNoRowsReturnsNullWithoutCallingMapper() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThat(jdbcTemplate.queryForObject(QUERY_SQL, rowMapper)).isNull();

        verifyNoInteractions(rowMapper);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsTheOnlyMappedObjectAndForwardsArguments() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet, 0)).thenReturn("alice");

        assertThat(jdbcTemplate.queryForObject(QUERY_SQL, rowMapper, 20, true)).isEqualTo("alice");

        InOrder order = inOrder(statement);
        order.verify(statement).setObject(1, 20);
        order.verify(statement).setObject(2, true);
        order.verify(statement).executeQuery();
        verify(rowMapper).mapRow(resultSet, 0);
        verifyNoMoreInteractions(rowMapper);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectRejectsMultipleRowsAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet, 0)).thenReturn("alice");
        when(rowMapper.mapRow(resultSet, 1)).thenReturn("bob");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(QUERY_SQL, rowMapper))
                .isInstanceOf(DataAccessException.class);

        verifyQueryResourcesClosed();
    }

    @Test
    void updateConvertsConnectionAcquisitionFailureAndPreservesCause() throws SQLException {
        SQLException failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyNoInteractions(connection, statement, resultSet);
    }

    @Test
    void queryConvertsConnectionAcquisitionFailureAndPreservesCause() throws SQLException {
        SQLException failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY_SQL, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyNoInteractions(connection, statement, resultSet, rowMapper);
    }

    @Test
    void updateClosesConnectionWhenStatementPreparationFails() throws SQLException {
        SQLException failure = new SQLException("cannot prepare update");
        when(connection.prepareStatement(UPDATE_SQL)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).close();
        verifyNoInteractions(statement, resultSet);
    }

    @Test
    void queryClosesConnectionWhenStatementPreparationFails() throws SQLException {
        SQLException failure = new SQLException("cannot prepare query");
        when(connection.prepareStatement(QUERY_SQL)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY_SQL, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).close();
        verifyNoInteractions(statement, resultSet, rowMapper);
    }

    @Test
    void updateClosesResourcesWhenBindingFailsWithoutExecutingStatement() throws SQLException {
        SQLException failure = new SQLException("cannot bind");
        doThrow(failure).when(statement).setObject(1, "alice");

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "alice"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement, never()).executeUpdate();
        verifyStatementAndConnectionClosed();
    }

    @Test
    void queryClosesResourcesWhenBindingFailsWithoutExecutingStatement() throws SQLException {
        SQLException failure = new SQLException("cannot bind");
        doThrow(failure).when(statement).setObject(1, 20);

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY_SQL, rowMapper, 20))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement, never()).executeQuery();
        verifyNoInteractions(resultSet, rowMapper);
        verifyStatementAndConnectionClosed();
    }

    @Test
    void updateConvertsExecutionFailureAndClosesResources() throws SQLException {
        SQLException failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyStatementAndConnectionClosed();
    }

    @Test
    void queryConvertsExecutionFailureAndClosesAcquiredResources() throws SQLException {
        SQLException failure = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY_SQL, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyNoInteractions(resultSet, rowMapper);
        verifyStatementAndConnectionClosed();
    }

    @Test
    void queryConvertsIterationFailureAndClosesAllResources() throws SQLException {
        SQLException failure = new SQLException("iteration failed");
        when(resultSet.next()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY_SQL, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyNoInteractions(rowMapper);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryConvertsMapperSqlExceptionAndClosesAllResources() throws SQLException {
        SQLException failure = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet, 0)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY_SQL, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    void queryPropagatesMapperRuntimeExceptionAndStillClosesAllResources() throws SQLException {
        IllegalArgumentException failure = new IllegalArgumentException("invalid row");
        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet, 0)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY_SQL, rowMapper)).isSameAs(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectConvertsMapperSqlExceptionAndClosesAllResources() throws SQLException {
        SQLException failure = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet, 0)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(QUERY_SQL, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    void queryClosesRemainingResourcesWhenResultSetCloseFails() throws SQLException {
        SQLException failure = new SQLException("cannot close result set");
        when(resultSet.next()).thenReturn(false);
        doThrow(failure).when(resultSet).close();

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY_SQL, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    void updateClosesConnectionWhenStatementCloseFails() throws SQLException {
        SQLException failure = new SQLException("cannot close statement");
        doThrow(failure).when(statement).close();

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyStatementAndConnectionClosed();
    }

    @Test
    void queryPreservesPrimarySqlExceptionAndSuppressedCloseException() throws SQLException {
        SQLException mappingFailure = new SQLException("mapping failed");
        SQLException closeFailure = new SQLException("cannot close result set");
        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet, 0)).thenThrow(mappingFailure);
        doThrow(closeFailure).when(resultSet).close();

        assertThatThrownBy(() -> jdbcTemplate.query(QUERY_SQL, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(mappingFailure);
        assertThat(mappingFailure.getSuppressed()).containsExactly(closeFailure);

        verifyQueryResourcesClosed();
    }

    private void verifyStatementAndConnectionClosed() throws SQLException {
        InOrder order = inOrder(statement, connection);
        order.verify(statement).close();
        order.verify(connection).close();
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        InOrder order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }
}
