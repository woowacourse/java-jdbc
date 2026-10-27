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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private static final String SQL = "select account from users where id = ?";

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
    }

    @Test
    void updateBindsParametersAndClosesResources() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update(SQL, "gugu", null, 1L)).isEqualTo(1);

        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, null);
        verify(statement).setObject(3, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMapsEveryRowAndClosesResources() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "stark");

        final var results = jdbcTemplate.query(SQL,
                (rs, rowNum) -> rowNum + ":" + rs.getString("account"), 1L);

        assertThat(results).containsExactly("0:gugu", "1:stark");
        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryReturnsEmptyListWhenNoRowsExist() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);

        assertThat(jdbcTemplate.query(SQL, (rs, rowNum) -> rs.getString(1))).isEmpty();
    }

    @Test
    void queryForObjectReturnsOneRow() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String result = jdbcTemplate.queryForObject(SQL,
                (rs, rowNum) -> rs.getString("account"), 1L);

        assertThat(result).isEqualTo("gugu");
    }

    @Test
    void queryForObjectReturnsNullWhenNoRowsExist() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);

        final String result = jdbcTemplate.queryForObject(SQL, (rs, rowNum) -> rs.getString(1));

        assertThat(result).isNull();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, (rs, rowNum) -> rowNum))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void updatePreservesSqlExceptionAndClosesResources() throws SQLException {
        final var cause = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void mappingFailureClosesAllResources() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        final var cause = new SQLException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, (rs, rowNum) -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void statementCreationFailureClosesConnection() throws SQLException {
        final var cause = new SQLException("prepare failed");
        when(connection.prepareStatement(SQL)).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
    }
}
