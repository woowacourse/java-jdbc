package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "update users set password = ? where id = ?";
    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @Test
    void borrowedConnectionRemainsOpenAfterUpdate() throws SQLException {
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update(connection, SQL, "changed", 1L)).isEqualTo(1);

        verify(statement).setObject(1, "changed");
        verify(statement).setObject(2, 1L);
        verify(statement).close();
        verify(connection, never()).close();
        verifyNoInteractions(dataSource);
    }

    @Test
    void borrowedConnectionRemainsOpenAfterFailure() throws SQLException {
        final var cause = new SQLException("update failed");
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, SQL, "changed", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection, never()).close();
    }
}
