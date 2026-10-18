package com.interface21.jdbc.core;

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
import static org.mockito.Mockito.mock;
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
    void closesResourcesWhenExecutionFails() throws SQLException {
        final var exception = new SQLException("SQL execution failed");
        when(pstmt.executeUpdate()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(RuntimeException.class)
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
                .isInstanceOf(RuntimeException.class)
                .hasCause(exception);

        verify(rs).close();
        verify(pstmt).close();
        verify(connection).close();
    }
}
