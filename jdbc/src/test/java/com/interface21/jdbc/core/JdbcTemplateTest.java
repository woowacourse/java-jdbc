package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    @Test
    @DisplayName("수정 SQL 실행이 실패하면 원인을 보존하고 자원을 닫는다")
    void updateFailureClosesResources() throws SQLException {
        SQLException cause = new SQLException("SQL 실행 실패");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users where id = ?", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("목록 조회의 결과 변환이 실패하면 원인을 보존하고 자원을 닫는다")
    void queryMappingFailureClosesResources() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        SQLException cause = new SQLException("조회 결과 변환 실패");
        RowMapper<String> rowMapper = rs -> {
            throw cause;
        };

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회의 결과 변환이 실패하면 원인을 보존하고 자원을 닫는다")
    void queryForObjectMappingFailureClosesResources() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        SQLException cause = new SQLException("조회 결과 변환 실패");
        RowMapper<String> rowMapper = rs -> {
            throw cause;
        };

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?", rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
