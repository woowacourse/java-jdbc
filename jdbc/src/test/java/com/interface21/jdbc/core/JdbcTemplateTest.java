package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String UPDATE_SQL = "update users set name = ? where id = ?";
    private static final String SELECT_SQL = "select name from users where id > ?";

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void updateBindsParametersAndClosesResources() throws SQLException {
        jdbcTemplate.update(UPDATE_SQL, "kaki", 1L);

        verify(preparedStatement).setObject(1, "kaki");
        verify(preparedStatement).setObject(2, 1L);
        verify(preparedStatement).executeUpdate();
        assertStatementAndConnectionClosed();
    }

    @Test
    void queryMapsEveryRowAndClosesResources() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("kaki", "gugu");

        final var names = jdbcTemplate.query(SELECT_SQL, row -> row.getString("name"), 0L);

        assertThat(names).containsExactly("kaki", "gugu");
        verify(resultSet).close();
        assertStatementAndConnectionClosed();
    }

    @Test
    void bindingFailurePreservesCauseAndClosesResources() throws SQLException {
        final var failure = new SQLException("binding failed");
        final PreparedStatementSetter setter = statement -> {
            throw failure;
        };

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, setter))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure)
                .hasMessageContaining(UPDATE_SQL);

        assertStatementAndConnectionClosed();
    }

    @Test
    void executionFailurePreservesCauseAndClosesResources() throws SQLException {
        final var failure = new SQLException("execution failed");
        when(preparedStatement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "kaki", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        assertStatementAndConnectionClosed();
    }

    @Test
    void mappingFailurePreservesCauseAndClosesAllResources() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        final var failure = new SQLException("mapping failed");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("name")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, row -> row.getString("name"), 0L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(resultSet).close();
        assertStatementAndConnectionClosed();
    }

    @Test
    void uncheckedMappingFailureClosesAllResources() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        final var failure = new IllegalStateException("invalid row");
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        final RowMapper<String> rowMapper = row -> {
            throw failure;
        };

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, rowMapper, 0L))
                .isSameAs(failure);

        verify(resultSet).close();
        assertStatementAndConnectionClosed();
    }

    @Test
    void statementPreparationFailureClosesConnection() throws SQLException {
        final var failure = new SQLException("prepare failed");
        when(connection.prepareStatement(UPDATE_SQL)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "kaki", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(connection).close();
    }

    @Test
    void connectionFailurePreservesCause() throws SQLException {
        final var failure = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "kaki", 1L))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(failure);
    }

    @Test
    void updateWithSuppliedConnectionClosesOnlyStatement() throws SQLException {
        jdbcTemplate.update(connection, UPDATE_SQL, statement -> {
            statement.setString(1, "kaki");
            statement.setLong(2, 1L);
        });

        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void queryWithSuppliedConnectionClosesResultSetAndStatement() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("kaki");

        final var names = jdbcTemplate.query(connection, SELECT_SQL, row -> row.getString("name"),
                statement -> statement.setLong(1, 0L));

        assertThat(names).containsExactly("kaki");
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void executionFailureLeavesSuppliedConnectionOpenForRollback() throws SQLException {
        final var failure = new SQLException("execution failed");
        when(preparedStatement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, UPDATE_SQL, "kaki", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(preparedStatement).close();
        verify(connection, never()).close();
        verify(dataSource, never()).getConnection();
    }

    @Test
    void closeFailureDoesNotHideSqlExecutionFailure() throws SQLException {
        final var failure = new SQLException("execution failed");
        final var closeFailure = new SQLException("close failed");
        when(preparedStatement.executeUpdate()).thenThrow(failure);
        doThrow(closeFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "kaki", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure)
                .satisfies(exception -> {
                    assertThat(exception.getSuppressed()).hasSize(1);
                    assertThat(exception.getSuppressed()[0]).hasCause(closeFailure);
                });
    }

    private void assertStatementAndConnectionClosed() throws SQLException {
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
