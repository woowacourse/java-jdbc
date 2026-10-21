package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String INSERT_SQL =
            "insert into users (account, password, email) values (?, ?, ?)";

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
    @DisplayName("인자를 1번부터 순서대로 바인딩하고 실행 후 자원을 닫는다")
    void update() throws SQLException {
        jdbcTemplate.update(INSERT_SQL, "gugu", "password", null);

        verify(connection).prepareStatement(INSERT_SQL);
        final var order = inOrder(statement, connection);
        order.verify(statement).setObject(1, "gugu");
        order.verify(statement).setObject(2, "password");
        order.verify(statement).setObject(3, null);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    @DisplayName("인자가 없는 SQL도 실행할 수 있다")
    void updateWithoutParameters() throws SQLException {
        final var sql = "delete from users";

        jdbcTemplate.update(sql);

        verify(connection).prepareStatement(sql);
        verify(statement, never()).setObject(anyInt(), any());
        verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("SQL 실행에 실패하면 원인을 보존한 예외를 던지고 자원을 닫는다")
    void updateFailsDuringExecution() throws SQLException {
        final var cause = new SQLException("SQL 실행 실패");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_SQL, "gugu", "password", "email"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("인자 바인딩에 실패하면 SQL을 실행하지 않고 자원을 닫는다")
    void updateFailsDuringParameterBinding() throws SQLException {
        final var cause = new SQLException("인자 바인딩 실패");
        doThrow(cause).when(statement).setObject(1, "gugu");

        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_SQL, "gugu", "password", "email"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("Statement 생성에 실패해도 이미 얻은 연결은 닫는다")
    void updateFailsDuringStatementCreation() throws SQLException {
        final var cause = new SQLException("Statement 생성 실패");
        when(connection.prepareStatement(INSERT_SQL)).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_SQL, "gugu", "password", "email"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
        verifyNoInteractions(statement);
    }

    @Test
    @DisplayName("연결을 얻지 못하면 원인을 보존한 예외를 던진다")
    void updateFailsDuringConnectionAcquisition() throws SQLException {
        final var cause = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_SQL, "gugu", "password", "email"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verifyNoInteractions(connection, statement);
    }
}
