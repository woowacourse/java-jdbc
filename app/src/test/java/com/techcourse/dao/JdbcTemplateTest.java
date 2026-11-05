package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

class JdbcTemplateTest {

    private static final String SQL = "update users set password = ? where id = ?";

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;
    private PreparedStatementSetter setter;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    @DisplayName("갱신과 조회가 동기화된 연결을 재사용하고 닫지 않는다")
    void reusesBoundConnectionAcrossUpdateAndQuery() throws SQLException {
        final var resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        jdbcTemplate.update(SQL, setter);
        jdbcTemplate.query(SQL, rs -> rs.getString("account"));

        verify(dataSource, never()).getConnection();
        verify(connection, never()).close();
        verify(resultSet).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("조회 실패 시에도 동기화된 연결을 닫지 않는다")
    void keepsBoundConnectionOpenOnFailure() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var cause = new SQLException("조회 실패");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement).close();
        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);

        jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };

        setter = statement -> {
            statement.setString(1, "password");
            statement.setLong(2, 1L);
        };
    }

    @Test
    @DisplayName("가변 인자를 순서대로 바인딩하며 null 값도 전달한다")
    void updateWithParameters() throws SQLException {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        jdbcTemplate.update(sql, "gugu", "password", null);

        final var order = inOrder(statement, connection);
        order.verify(statement).setObject(1, "gugu");
        order.verify(statement).setObject(2, "password");
        order.verify(statement).setObject(3, null);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    @DisplayName("가변 인자가 없어도 SQL을 실행한다")
    void updateWithoutParameters() throws SQLException {
        final var sql = "delete from users";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        jdbcTemplate.update(sql);

        verify(statement, never()).setObject(anyInt(), any());
        verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("값 설정에 실패하면 실행하지 않고 자원을 닫으며 원인 예외를 보존한다")
    void updateFailsDuringBinding() throws SQLException {
        final var cause = new SQLException("값 설정 실패");
        doThrow(cause).when(statement).setString(1, "password");

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, setter))
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

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, setter))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).executeUpdate();
        verify(connection).close();
    }

    @Test
    void updateWithExternalConnection() throws SQLException {
        jdbcTemplate.update(connection, SQL, "password", 1L);

        verify(statement).setObject(1, "password");
        verify(statement).setObject(2, 1L);
        verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verifyNoInteractions(dataSource);
    }

    @Test
    void externalConnectionStaysOpenWhenUpdateFails() throws SQLException {
        final var cause = new SQLException("수정 실패");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, SQL, setter))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement).close();
        verify(connection, never()).close();
        verify(connection, never()).rollback();
        verifyNoInteractions(dataSource);
    }
}
