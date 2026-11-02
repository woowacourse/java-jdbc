package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void queryUsesExecutionHookWhileResourcesAreOpen() throws SQLException {
        final var queryExecuted = new AtomicBoolean();
        final var template = new JdbcTemplate(dataSource) {
            @Override
            protected ResultSet executeQuery(PreparedStatementSetter statementSetter, PreparedStatement statement)
                    throws SQLException {
                queryExecuted.set(true);
                verify(connection, never()).close();
                verify(statement, never()).close();
                return super.executeQuery(statementSetter, statement);
            }
        };
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("keyboard");

        final var rows = template.query("select name from products where price > ?",
                statement -> statement.setInt(1, 1000), row -> row.getString("name"));

        assertThat(template.getDataSource()).isSameAs(dataSource);
        assertThat(queryExecuted).isTrue();
        assertThat(rows).containsExactly("keyboard");
        verify(preparedStatement).setInt(1, 1000);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryMapsEveryRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("first", "second");
        final var sql = "select name from products where price > ?";

        final var rows = jdbcTemplate.query(sql,
                row -> new ProductRow(row.getString("name")), 1000);

        assertThat(rows).containsExactly(new ProductRow("first"), new ProductRow("second"));
        final var executionOrder = inOrder(connection, preparedStatement, resultSet);
        executionOrder.verify(connection).prepareStatement(sql);
        executionOrder.verify(preparedStatement).setObject(1, 1000);
        executionOrder.verify(preparedStatement).executeQuery();
        executionOrder.verify(resultSet).close();
        executionOrder.verify(preparedStatement).close();
        executionOrder.verify(connection).close();
    }

    @Test
    void queryWithoutParametersReturnsEmptyListWhenNoRowsMatch() throws SQLException {
        final var rows = jdbcTemplate.query("select name from products", row -> {
            throw new AssertionError("빈 결과에는 RowMapper를 호출하지 않습니다.");
        });

        assertThat(rows).isEmpty();
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verifyNoMoreInteractions(preparedStatement);
        verify(resultSet).close();
        verify(connection).close();
    }

    @Test
    void queryAcceptsSetterAndMapperLambdas() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("keyboard");

        final var rows = jdbcTemplate.query("select name from products where price > ?",
                statement -> statement.setInt(1, 1000),
                row -> row.getString("name"));

        assertThat(rows).containsExactly("keyboard");
        final var executionOrder = inOrder(preparedStatement);
        executionOrder.verify(preparedStatement).setInt(1, 1000);
        executionOrder.verify(preparedStatement).executeQuery();
    }

    @Test
    void updateBindsArgumentsInOrderIncludingNull() throws SQLException {
        jdbcTemplate.update(
                "update products set name = ?, price = ? where description = ?", "keyboard", 1000, null);

        final var executionOrder = inOrder(preparedStatement, connection);
        executionOrder.verify(preparedStatement).setObject(1, "keyboard");
        executionOrder.verify(preparedStatement).setObject(2, 1000);
        executionOrder.verify(preparedStatement).setObject(3, null);
        executionOrder.verify(preparedStatement).executeUpdate();
        executionOrder.verify(preparedStatement).close();
        executionOrder.verify(connection).close();
        verifyNoInteractions(resultSet);
    }

    @Test
    void updateWithoutParametersExecutesStatement() throws SQLException {
        jdbcTemplate.update("delete from products");

        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verifyNoMoreInteractions(preparedStatement);
        verify(connection).close();
    }

    @Test
    void updateAcceptsSetterLambda() throws SQLException {
        jdbcTemplate.update("insert into products (name, description) values (?, ?)",
                statement -> {
                    statement.setString(1, "keyboard");
                    statement.setNull(2, Types.VARCHAR);
                });

        final var executionOrder = inOrder(preparedStatement);
        executionOrder.verify(preparedStatement).setString(1, "keyboard");
        executionOrder.verify(preparedStatement).setNull(2, Types.VARCHAR);
        executionOrder.verify(preparedStatement).executeUpdate();
    }

    @Test
    void updateAcceptsAnonymousSetter() throws SQLException {
        final var statementSetter = new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement) throws SQLException {
                statement.setString(1, "keyboard");
            }
        };

        jdbcTemplate.update("insert into products (name) values (?)", statementSetter);

        final var executionOrder = inOrder(preparedStatement);
        executionOrder.verify(preparedStatement).setString(1, "keyboard");
        executionOrder.verify(preparedStatement).executeUpdate();
        executionOrder.verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void queryTranslatesConnectionFailure() throws SQLException {
        final var cause = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> "name"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verifyNoInteractions(connection, preparedStatement, resultSet);
    }

    @Test
    void updateClosesConnectionWhenStatementPreparationFails() throws SQLException {
        final var cause = new SQLException("구문 준비 실패");
        when(connection.prepareStatement(anyString())).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("invalid sql"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
        verifyNoInteractions(preparedStatement, resultSet);
    }

    @Test
    void queryClosesResourcesWhenArgumentBindingFails() throws SQLException {
        final var cause = new SQLException("바인딩 실패");
        doThrow(cause).when(preparedStatement).setObject(1, 1000);

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select name from products where price > ?", row -> "name", 1000))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(preparedStatement, never()).executeQuery();
        verify(preparedStatement).close();
        verify(connection).close();
        verifyNoInteractions(resultSet);
    }

    @Test
    void updateClosesResourcesWhenSetterThrowsSQLException() throws SQLException {
        final var cause = new SQLException("설정 실패");

        assertThatThrownBy(() -> jdbcTemplate.update("insert into products (name) values (?)", statement -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(preparedStatement, never()).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void queryTranslatesSQLExceptionAndClosesResources() throws SQLException {
        final var cause = new SQLException("조회 실패");
        when(preparedStatement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select name from products where price > ?", row -> row.getString("name"), 1000))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(preparedStatement).close();
        verify(connection).close();
        verifyNoInteractions(resultSet);
    }

    @Test
    void updateTranslatesSQLExceptionAndClosesResources() throws SQLException {
        final var cause = new SQLException("수정 실패");
        when(preparedStatement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("update products set price = ?", 1000))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void queryClosesAllResourcesWhenRowIterationFails() throws SQLException {
        final var cause = new SQLException("행 탐색 실패");
        when(resultSet.next()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> "name"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void queryClosesAllResourcesWhenRowMappingFails() throws SQLException {
        final var cause = new SQLException("행 매핑 실패");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void queryPreservesUncheckedMapperExceptionAndClosesAllResources() throws SQLException {
        final var cause = new IllegalArgumentException("잘못된 상품");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> {
            throw cause;
        })).isSameAs(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void updatePreservesUncheckedSetterExceptionAndClosesResources() throws SQLException {
        final var cause = new IllegalArgumentException("잘못된 매개변수");

        assertThatThrownBy(() -> jdbcTemplate.update("insert into products (name) values (?)", statement -> {
            throw cause;
        })).isSameAs(cause);

        verify(preparedStatement, never()).executeUpdate();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    void queryTranslatesResultSetCloseFailureAndClosesRemainingResources() throws SQLException {
        final var cause = new SQLException("결과 반환 실패");
        doThrow(cause).when(resultSet).close();

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> "name"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void updateTranslatesStatementCloseFailureAndClosesConnection() throws SQLException {
        final var cause = new SQLException("구문 반환 실패");
        doThrow(cause).when(preparedStatement).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from products"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
    }

    @Test
    void updateTranslatesConnectionCloseFailure() throws SQLException {
        final var cause = new SQLException("연결 반환 실패");
        doThrow(cause).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from products"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    void queryPreservesOriginalFailureWhenResourceClosingAlsoFails() throws SQLException {
        final var cause = new SQLException("행 매핑 실패");
        final var resultSetCloseFailure = new SQLException("결과 반환 실패");
        final var statementCloseFailure = new SQLException("구문 반환 실패");
        final var connectionCloseFailure = new SQLException("연결 반환 실패");
        when(resultSet.next()).thenReturn(true);
        doThrow(resultSetCloseFailure).when(resultSet).close();
        doThrow(statementCloseFailure).when(preparedStatement).close();
        doThrow(connectionCloseFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        assertThat(cause.getSuppressed())
                .containsExactly(resultSetCloseFailure, statementCloseFailure, connectionCloseFailure);
        verifyQueryResourcesClosed();
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        final var closeOrder = inOrder(resultSet, preparedStatement, connection);
        closeOrder.verify(resultSet).close();
        closeOrder.verify(preparedStatement).close();
        closeOrder.verify(connection).close();
    }

    private record ProductRow(String name) {
    }
}
