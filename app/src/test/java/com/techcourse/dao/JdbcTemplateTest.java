package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "update users set password = ? where id = ?";

    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);

        jdbcTemplate = new JdbcTemplate() {
            @Override
            protected String createQuery() {
                return SQL;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setString(1, "password");
                statement.setLong(2, 1L);
            }
        };
    }

    @Test
    @DisplayName("값 설정에 실패하면 실행하지 않고 자원을 닫으며 원인 예외를 보존한다")
    void updateFailsDuringBinding() throws SQLException {
        final var cause = new SQLException("값 설정 실패");
        doThrow(cause).when(statement).setString(1, "password");

        assertThatThrownBy(() -> jdbcTemplate.update())
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("Statement 정리에 실패해도 연결을 닫고 정리 실패를 전달한다")
    void updateFailsWhenClosingStatement() throws SQLException {
        final var cause = new SQLException("Statement 정리 실패");
        doThrow(cause).when(statement).close();

        assertThatThrownBy(() -> jdbcTemplate.update())
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).executeUpdate();
        verify(connection).close();
    }
}
