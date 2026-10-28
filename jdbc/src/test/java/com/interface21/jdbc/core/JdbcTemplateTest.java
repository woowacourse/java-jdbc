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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private PreparedStatement statement;
    private SQLException closeFailure;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("sql")).thenReturn(statement);
        closeFailure = new SQLException("close failed");
        doThrow(closeFailure).when(connection).close();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void queryPreservesSqlFailureWhenReleaseFails() throws SQLException {
        final var sqlFailure = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(sqlFailure);

        final var failure = assertThrows(DataAccessException.class,
                () -> jdbcTemplate.query("sql", resultSet -> resultSet.getString(1)));

        assertThat(failure).hasCause(sqlFailure);
        assertReleaseFailureSuppressed(failure);
    }

    @Test
    void updatePreservesSqlFailureWhenReleaseFails() throws SQLException {
        final var sqlFailure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(sqlFailure);

        final var failure = assertThrows(DataAccessException.class, () -> jdbcTemplate.update("sql"));

        assertThat(failure).hasCause(sqlFailure);
        assertReleaseFailureSuppressed(failure);
    }

    @Test
    void queryPreservesRuntimeFailureWhenReleaseFails() {
        final var originalFailure = new IllegalArgumentException("invalid parameter");

        final var failure = assertThrows(IllegalArgumentException.class,
                () -> jdbcTemplate.query("sql", resultSet -> resultSet.getString(1), preparedStatement -> {
                    throw originalFailure;
                }));

        assertThat(failure).isSameAs(originalFailure);
        assertReleaseFailureSuppressed(failure);
    }

    @Test
    void updatePreservesRuntimeFailureWhenReleaseFails() {
        final var originalFailure = new IllegalArgumentException("invalid parameter");

        final var failure = assertThrows(IllegalArgumentException.class,
                () -> jdbcTemplate.update("sql", preparedStatement -> {
                    throw originalFailure;
                }));

        assertThat(failure).isSameAs(originalFailure);
        assertReleaseFailureSuppressed(failure);
    }

    @Test
    void queryPreservesErrorWhenReleaseFails() {
        final var originalFailure = new AssertionError("binding failed");

        final var failure = assertThrows(AssertionError.class,
                () -> jdbcTemplate.query("sql", resultSet -> resultSet.getString(1), preparedStatement -> {
                    throw originalFailure;
                }));

        assertThat(failure).isSameAs(originalFailure);
        assertReleaseFailureSuppressed(failure);
    }

    @Test
    void updatePreservesErrorWhenReleaseFails() {
        final var originalFailure = new AssertionError("binding failed");

        final var failure = assertThrows(AssertionError.class,
                () -> jdbcTemplate.update("sql", preparedStatement -> {
                    throw originalFailure;
                }));

        assertThat(failure).isSameAs(originalFailure);
        assertReleaseFailureSuppressed(failure);
    }

    @Test
    void queryPropagatesReleaseFailureAfterSuccess() throws SQLException {
        when(statement.executeQuery()).thenReturn(mock(ResultSet.class));

        final var failure = assertThrows(CannotGetJdbcConnectionException.class,
                () -> jdbcTemplate.query("sql", resultSet -> resultSet.getString(1)));

        assertThat(failure).hasCause(closeFailure).hasNoSuppressedExceptions();
    }

    @Test
    void updatePropagatesReleaseFailureAfterSuccess() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final var failure = assertThrows(CannotGetJdbcConnectionException.class,
                () -> jdbcTemplate.update("sql"));

        assertThat(failure).hasCause(closeFailure).hasNoSuppressedExceptions();
    }

    private void assertReleaseFailureSuppressed(final Throwable failure) {
        assertThat(failure.getSuppressed()).singleElement().satisfies(suppressed ->
                assertThat(suppressed).isInstanceOf(CannotGetJdbcConnectionException.class).hasCause(closeFailure));
    }
}
