package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DuplicateKeyException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
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

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement pstmt = mock(PreparedStatement.class);
    private final ResultSet rs = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    void exposesConnectionAcquisitionFailureAsDataAccessException() throws SQLException {
        final var exception = new SQLException("Cannot obtain connection");
        when(dataSource.getConnection()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(connection, never()).close();
    }

    @Test
    void preservesSqlFailureWhenConnectionReleaseAlsoFails() throws SQLException {
        final var sqlException = new SQLException("SQL execution failed");
        final var closeException = new SQLException("Connection close failed");
        when(pstmt.executeUpdate()).thenThrow(sqlException);
        doThrow(closeException).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(sqlException)
                .satisfies(exception -> {
                    assertThat(exception.getSuppressed()).hasSize(1);
                    assertThat(exception.getSuppressed()[0]).hasCause(closeException);
                });
    }

    @Test
    void preservesErrorWhenConnectionReleaseAlsoFails() throws SQLException {
        final var error = new AssertionError("Execution failed");
        final var closeException = new SQLException("Connection close failed");
        when(pstmt.executeUpdate()).thenThrow(error);
        doThrow(closeException).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isSameAs(error)
                .satisfies(exception -> {
                    assertThat(exception.getSuppressed()).hasSize(1);
                    assertThat(exception.getSuppressed()[0]).hasCause(closeException);
                });
    }

    @Test
    void reportsReleaseFailureWhenSqlSucceeds() throws SQLException {
        final var closeException = new SQLException("Connection close failed");
        when(pstmt.executeUpdate()).thenReturn(1);
        doThrow(closeException).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(CannotGetJdbcConnectionException.class)
                .hasCause(closeException);
    }

    @Test
    void update() throws SQLException {
        final var sql = "update users set password = ? where id = ?";
        when(pstmt.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(sql, "password99", 1L);

        assertThat(affectedRows).isEqualTo(1);
        verify(connection).prepareStatement(sql);
        verify(pstmt).setObject(1, "password99");
        verify(pstmt).setObject(2, 1L);
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void updateUsesBoundConnectionWithoutClosingIt() throws SQLException {
        when(pstmt.executeUpdate()).thenReturn(1);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var affectedRows = jdbcTemplate.update(
                "update users set password = ? where id = ?", "password99", 1L);

        assertThat(affectedRows).isEqualTo(1);
        verify(pstmt).setObject(1, "password99");
        verify(pstmt).setObject(2, 1L);
        verify(pstmt).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }

    @Test
    void keepsBoundConnectionOpenWhenExecutionFails() throws SQLException {
        final var exception = new SQLException("SQL execution failed");
        when(pstmt.executeUpdate()).thenThrow(exception);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(pstmt).close();
        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
    }

    @Test
    void closesResourcesWhenExecutionFails() throws SQLException {
        final var exception = new SQLException("SQL execution failed");
        when(pstmt.executeUpdate()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void updateTranslatesDuplicateKeyException() throws SQLException {
        final var exception = new SQLException("Duplicate key", "23505");
        when(pstmt.executeUpdate()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("insert into users (account) values (?)", "gugu"))
                .isInstanceOf(DuplicateKeyException.class)
                .hasCause(exception);

        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void queryForObject() throws SQLException {
        final var sql = "select account from users where id = ?";
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("account")).thenReturn("gugu");

        final var account = jdbcTemplate.queryForObject(sql, row -> row.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
        verify(pstmt).setObject(1, 1L);
    }

    @Test
    void queryForObjectReturnsNullWhenNoRows() throws SQLException {
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        final var account = jdbcTemplate.queryForObject("select account from users", row -> row.getString("account"));

        assertThat(account).isNull();
    }

    @Test
    void queryForObjectFailsWhenMultipleRows() throws SQLException {
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("account")).thenReturn("gugu", "choco");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users", row -> row.getString("account")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void query() throws SQLException {
        final var sql = "select account from users where id > ?";
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getString("account")).thenReturn("gugu", "choco");

        final var accounts = jdbcTemplate.query(sql, row -> row.getString("account"), 0L);

        assertThat(accounts).containsExactly("gugu", "choco");
        verify(connection).prepareStatement(sql);
        verify(pstmt).setObject(1, 0L);
        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void queryReturnsEmptyListWhenNoRows() throws SQLException {
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        final var accounts = jdbcTemplate.query("select account from users", row -> row.getString("account"));

        assertThat(accounts).isEmpty();
    }

    @Test
    void closesResourcesWhenMappingFails() throws SQLException {
        final var exception = new SQLException("Row mapping failed");
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getString("account")).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void closesConnectionWhenStatementCreationFails() throws SQLException {
        final var exception = new SQLException("Cannot prepare statement");
        when(connection.prepareStatement(anyString())).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(connection).close();
    }

    @Test
    void closesResourcesWhenBindingFails() throws SQLException {
        final var exception = new SQLException("Cannot bind parameter");
        doThrow(exception).when(pstmt).setObject(1, "gugu");

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users where account = ?",
                row -> row.getString("account"), "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(pstmt, never()).executeQuery();
        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void closesResourcesWhenQueryExecutionFails() throws SQLException {
        final var exception = new SQLException("Cannot execute query");
        when(pstmt.executeQuery()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", row -> row.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(pstmt).close();
        verify(connection).close();
    }

    @Test
    void closesResourcesWhenMapperThrowsUncheckedException() throws SQLException {
        final var exception = new IllegalArgumentException("Invalid row");
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", row -> {
            throw exception;
        })).isSameAs(exception);

        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }
}
