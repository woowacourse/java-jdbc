package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final int affected = jdbcTemplate.update("update users set account = ? where id = ?", "gugu", 1L);

        assertThat(affected).isEqualTo(1);
        verify(preparedStatement).setObject(1, "gugu");
        verify(preparedStatement).setObject(2, 1L);
        verifyStatementAndConnectionClosed();
    }

    @Test
    void updateWithPreparedStatementSetter() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final int affected = jdbcTemplate.update("update users set email = ? where id = ?", pstmt -> {
            pstmt.setNull(1, Types.VARCHAR);
            pstmt.setLong(2, 1L);
        });

        assertThat(affected).isEqualTo(1);
        verify(preparedStatement).setNull(1, Types.VARCHAR);
        verify(preparedStatement).setLong(2, 1L);
        verifyStatementAndConnectionClosed();
    }

    @Test
    void query() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "hkkang");

        final var accounts = jdbcTemplate.query("select account from users", rs -> rs.getString("account"));

        assertThat(accounts).containsExactly("gugu", "hkkang");
        verifyQueryResourcesClosed();
    }

    @Test
    void queryWithPreparedStatementSetter() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var accounts = jdbcTemplate.query("select account from users where id = ?",
                pstmt -> pstmt.setLong(1, 1L), rs -> rs.getString("account"));

        assertThat(accounts).containsExactly("gugu");
        verify(preparedStatement).setLong(1, 1L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObject() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String account = jdbcTemplate.queryForObject("select account from users where id = ?",
                rs -> rs.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
        verify(preparedStatement).setObject(1, 1L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectWithPreparedStatementSetter() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);

        final Integer id = jdbcTemplate.queryForObject("select id from users where account = ?",
                pstmt -> pstmt.setString(1, "gugu"), rs -> rs.getInt("id"));

        assertThat(id).isEqualTo(1);
        verify(preparedStatement).setString(1, "gugu");
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectRejectsEmptyResult() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users",
                rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("조회 결과가 없습니다.");

        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users",
                rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("size : 2");

        verifyQueryResourcesClosed();
    }

    @Test
    void translateRowMappingFailureAndCloseResources() throws SQLException {
        final var cause = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rs -> { throw cause; }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void translateSQLExceptionAndCloseResources() throws SQLException {
        final var cause = new SQLException("boom");
        when(preparedStatement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verifyStatementAndConnectionClosed();
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verifyStatementAndConnectionClosed();
    }

    private void verifyStatementAndConnectionClosed() throws SQLException {
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
