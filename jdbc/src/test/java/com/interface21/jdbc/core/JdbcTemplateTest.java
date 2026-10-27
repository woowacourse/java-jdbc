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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final RowMapper<String> ACCOUNT_MAPPER = rs -> rs.getString("account");

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
    void update() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update("update users set account = ? where id = ?", "gugu", 1L);

        assertThat(affectedRows).isEqualTo(1);
        verify(preparedStatement).setObject(1, "gugu");
        verify(preparedStatement).setObject(2, 1L);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void updateThrowsDataAccessException() throws SQLException {
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException());

        assertThatThrownBy(() -> jdbcTemplate.update("update users set account = ?", "gugu"))
                .isInstanceOf(DataAccessException.class);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void query() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "sun");

        final var accounts = jdbcTemplate.query("select account from users where email = ?", ACCOUNT_MAPPER, "a@b.com");

        assertThat(accounts).containsExactly("gugu", "sun");
        verify(preparedStatement).setObject(1, "a@b.com");
        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void queryForObject() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var account = jdbcTemplate.queryForObject("select account from users where id = ?", ACCOUNT_MAPPER, 1L);

        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void queryForObjectThrowsWhenResultIsEmpty() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users where id = ?", ACCOUNT_MAPPER, 1L))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void queryForObjectThrowsWhenResultIsMoreThanOne() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users", ACCOUNT_MAPPER))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void updateWithPreparedStatementSetter() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update("update users set account = ? where id = ?", pstmt -> {
            pstmt.setString(1, "gugu");
            pstmt.setLong(2, 1L);
        });

        assertThat(affectedRows).isEqualTo(1);
        verify(preparedStatement).setString(1, "gugu");
        verify(preparedStatement).setLong(2, 1L);
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void queryWithPreparedStatementSetter() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var accounts = jdbcTemplate.query("select account from users where id = ?", ACCOUNT_MAPPER,
                pstmt -> pstmt.setLong(1, 1L));

        assertThat(accounts).containsExactly("gugu");
        verify(preparedStatement).setLong(1, 1L);
    }
}
