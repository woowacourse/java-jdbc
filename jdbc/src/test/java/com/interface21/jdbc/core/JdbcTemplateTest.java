package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

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
}
