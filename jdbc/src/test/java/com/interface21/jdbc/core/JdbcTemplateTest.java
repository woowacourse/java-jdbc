package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    @Test
    void 인자를_순서대로_바인딩하고_영향받은_행_수를_반환한다() throws SQLException {
        final var sql = "update users set password = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(1);

        final var affectedRows = jdbcTemplate.update(sql, "password99", 1L);

        assertThat(affectedRows).isEqualTo(1);
        final var order = inOrder(connection, statement);
        order.verify(connection).prepareStatement(sql);
        order.verify(statement).setObject(1, "password99");
        order.verify(statement).setObject(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 인자가_없는_SQL도_실행한다() throws SQLException {
        when(statement.executeUpdate()).thenReturn(3);

        final var affectedRows = jdbcTemplate.update("delete from users");

        assertThat(affectedRows).isEqualTo(3);
        verify(statement, never()).setObject(anyInt(), any());
        verify(statement).executeUpdate();
    }

    @Test
    void 실행에_실패하면_자원을_닫고_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("쿼리 실행 실패");
        when(statement.executeUpdate()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(RuntimeException.class)
                .hasCause(exception);

        final var order = inOrder(statement, connection);
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 구문_준비에_실패해도_연결을_닫는다() throws SQLException {
        final var exception = new SQLException("구문 준비 실패");
        when(connection.prepareStatement(anyString())).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(RuntimeException.class)
                .hasCause(exception);

        verify(connection).close();
    }

    @Test
    void 연결에_실패하면_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(RuntimeException.class)
                .hasCause(exception);

        verify(connection, never()).prepareStatement(anyString());
    }
}
