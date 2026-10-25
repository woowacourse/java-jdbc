package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        final var connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void queryTranslatesSQLException() throws SQLException {
        final var cause = new SQLException("조회 실패");
        when(preparedStatement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select id from users where id = ?", (rs, rowNum) -> rs.getLong("id"), 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    void updateTranslatesSQLException() throws SQLException {
        final var cause = new SQLException("수정 실패");
        when(preparedStatement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "update users set password = ? where id = ?", "new-password", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }
}
