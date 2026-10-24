package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private static final String SQL = "select name from users where id = ?";

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
    }

    @Test
    void updateBindsArgumentsAndReturnsAffectedRows() throws SQLException {
        final var sql = "update users set name = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update(sql, "gugu", 7L)).isEqualTo(1);

        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, 7L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateFailurePreservesCauseAndClosesResources() throws SQLException {
        final var sql = "delete from users";
        final var cause = new SQLException("update failed");
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(RuntimeException.class).hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

}
