package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final String SQL = "select account from users where email = ?";
    private static final String EMAIL = "user@example.com";

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private final RowMapper<String> rowMapper = rs -> rs.getString("account");

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void queryForObjectReturnsExactlyOneRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        String account = jdbcTemplate.queryForObject(SQL, rowMapper, EMAIL);

        assertThat(account).isEqualTo("gugu");
        verify(statement).setObject(1, EMAIL);
    }

    @Test
    void queryForObjectThrowsWhenNoRowsAreReturned() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rowMapper, EMAIL))
                .isInstanceOfSatisfying(IncorrectResultSizeDataAccessException.class, exception -> {
                    assertThat(exception.getExpectedSize()).isEqualTo(1);
                    assertThat(exception.getActualSize()).isZero();
                });
    }

    @Test
    void queryForObjectThrowsWhenMultipleRowsAreReturned() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rowMapper, EMAIL))
                .isInstanceOfSatisfying(IncorrectResultSizeDataAccessException.class, exception -> {
                    assertThat(exception.getExpectedSize()).isEqualTo(1);
                    assertThat(exception.getActualSize()).isEqualTo(2);
                });
    }

    @Test
    void updateTranslatesSqlExceptionAndPreservesCause() throws SQLException {
        String updateSql = "update users set email = ? where account = ?";
        SQLException cause = new SQLException("Update failed");
        when(connection.prepareStatement(updateSql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(updateSql, EMAIL, "gugu"))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryTranslatesSqlExceptionAndPreservesCause() throws SQLException {
        SQLException cause = new SQLException("Query failed");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rowMapper, EMAIL))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateReturnsAffectedRowCountAndBindsParameters() throws SQLException {
        String updateSql = "update users set email = ? where id = ?";
        when(connection.prepareStatement(updateSql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(3);

        int affectedRows = jdbcTemplate.update(updateSql, EMAIL, 1L);

        assertThat(affectedRows).isEqualTo(3);
        verify(statement).setObject(1, EMAIL);
        verify(statement).setObject(2, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryMapsAllRowsAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "other");

        var accounts = jdbcTemplate.query(SQL, rowMapper, EMAIL);

        assertThat(accounts).containsExactly("gugu", "other");
        verify(statement).setObject(1, EMAIL);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void bindingFailurePreservesCauseAndClosesResources() throws SQLException {
        String updateSql = "update users set email = ? where id = ?";
        SQLException cause = new SQLException("Binding failed");
        when(connection.prepareStatement(updateSql)).thenReturn(statement);
        doThrow(cause).when(statement).setObject(1, EMAIL);

        assertThatThrownBy(() -> jdbcTemplate.update(updateSql, EMAIL, 1L))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void mappingFailurePreservesCauseAndClosesAllResources() throws SQLException {
        SQLException cause = new SQLException("Mapping failed");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rowMapper, EMAIL))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
