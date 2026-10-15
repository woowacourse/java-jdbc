package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
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
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
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

    @Test
    void 조회_인자를_바인딩하고_각_행을_변환해_반환한다() throws SQLException {
        final String sql = "select account from users where email = ? and id > ?";
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        final var accounts = jdbcTemplate.query(
                sql,
                row -> row.getString("account"),
                "test@woowahan.com",
                0L
        );

        assertThat(accounts).containsExactly("gugu", "pobi");
        verify(connection).prepareStatement(sql);
        final var order = inOrder(statement, resultSet, connection);
        order.verify(statement).setObject(1, "test@woowahan.com");
        order.verify(statement).setObject(2, 0L);
        order.verify(statement).executeQuery();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 조회_결과가_없으면_빈_리스트를_반환한다() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final var accounts = jdbcTemplate.query(
                "select account from users",
                row -> row.getString("account")
        );

        assertThat(accounts).isEmpty();
        verify(resultSet, never()).getString(anyString());
    }

    @Test
    void 조회_실행에_실패하면_자원을_닫고_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("조회 실행 실패");
        when(statement.executeQuery()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select account from users",
                row -> row.getString("account")
        ))
                .isInstanceOf(RuntimeException.class)
                .hasCause(exception);

        final var order = inOrder(statement, connection);
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    void 행_변환에_실패하면_모든_자원을_닫고_원인_예외를_보존한다() throws SQLException {
        final var exception = new SQLException("행 변환 실패");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select account from users",
                row -> row.getString("account")
        ))
                .isInstanceOf(RuntimeException.class)
                .hasCause(exception);

        final var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }
}
