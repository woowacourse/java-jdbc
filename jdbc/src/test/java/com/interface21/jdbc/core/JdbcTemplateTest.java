package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    @Test
    void wrapsSqlExceptionAndPreservesCause() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        final var sql = "invalid sql";
        final var cause = new SQLSyntaxErrorException("Invalid SQL syntax");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenThrow(cause);
        final var jdbcTemplate = new JdbcTemplate(dataSource);

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(cause);
    }
}
