package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    @Test
    @DisplayName("업데이트성 쿼리를 올바르게 수행한다.")
    void validUpdate() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("update test set name = ? where id = ?")).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(5);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        int rowsAffected = jdbcTemplate.update("update test set name = ? where id = ?", "test", 1);

        assertThat(rowsAffected).isEqualTo(5);
        verify(preparedStatement).setObject(1, "test");
        verify(preparedStatement).setObject(2, 1);
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
