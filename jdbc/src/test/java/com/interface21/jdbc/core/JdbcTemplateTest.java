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

    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final int affected = jdbcTemplate.update("update users set account = ? where id = ?", "gugu", 1L);

        assertThat(affected).isEqualTo(1);
        verify(preparedStatement).setObject(1, "gugu");
        verify(preparedStatement).setObject(2, 1L);
    }

    @Test
    void query() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "hkkang");

        final var accounts = jdbcTemplate.query("select account from users", rs -> rs.getString("account"));

        assertThat(accounts).containsExactly("gugu", "hkkang");
        verify(resultSet).close();
    }

    @Test
    void translateSQLExceptionAndCloseResources() throws SQLException {
        final var cause = new SQLException("boom");
        when(preparedStatement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
