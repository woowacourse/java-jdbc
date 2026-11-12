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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "select account from users where id = ?";

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;

    @BeforeEach
    void setup() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void updateBindsParametersAndReturnsAffectedRows() throws SQLException {
        final String sql = "update users set account = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        final int updatedRows = jdbcTemplate.update(sql, "gugu", 1L);

        assertThat(updatedRows).isEqualTo(1);
        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateUsesSuppliedConnectionWithoutClosingIt() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int updatedRows = jdbcTemplate.update(connection, SQL, "gugu", 1L);

        assertThat(updatedRows).isEqualTo(1);
        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, 1L);
        verify(statement).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @Test
    void updateFailureLeavesSuppliedConnectionOpenForRollback() throws SQLException {
        final SQLException cause = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(statement).close();
        verify(connection, never()).close();
    }

    @Test
    void queryMapsAllRowsAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "juni");

        final var accounts = jdbcTemplate.query(SQL, rs -> rs.getString("account"), 1L);

        assertThat(accounts).containsExactly("gugu", "juni");
        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void connectionFailureIsWrappedInDataAccessException() throws SQLException {
        final SQLException cause = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    void statementPreparationFailureClosesConnection() throws SQLException {
        final SQLException cause = new SQLException("prepare failed");
        when(connection.prepareStatement(SQL)).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(connection).close();
    }

    @Test
    void updateFailureClosesResources() throws SQLException {
        final SQLException cause = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void parameterBindingFailureClosesResources() throws SQLException {
        final SQLException cause = new SQLException("binding failed");
        doThrow(cause).when(statement).setObject(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rs -> rs.getString("account"), 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryExecutionFailureClosesResources() throws SQLException {
        final SQLException cause = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rs -> rs.getString("account"), 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void rowMappingFailureClosesResources() throws SQLException {
        final SQLException cause = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectWithNoRows() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rs -> rs.getString("account"), 1L))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("조회 결과는 1건이어야 합니다: 0");
    }

    @Test
    void queryForObjectWithOneRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String account = jdbcTemplate.queryForObject(SQL, rs -> rs.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void queryForObjectWithMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rs -> rs.getString("account"), 1L))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("조회 결과는 1건이어야 합니다: 2");
    }
}
