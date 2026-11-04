package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JdbcTemplate 단위 테스트")
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
    @DisplayName("자원이 열린 상태에서 조회 실행 훅을 호출한다")
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
    @DisplayName("조회한 모든 행을 순서대로 매핑하고 자원을 닫는다")
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
    @DisplayName("매개변수 없이 조회한 결과가 없으면 빈 목록을 반환한다")
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
    @DisplayName("값 설정과 행 매핑 람다로 조회한다")
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
    @DisplayName("수정 시 null을 포함한 인자를 순서대로 바인딩한다")
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
    @DisplayName("매개변수 없는 수정 SQL을 실행한다")
    void updateWithoutParametersExecutesStatement() throws SQLException {
        jdbcTemplate.update("delete from products");

        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verifyNoMoreInteractions(preparedStatement);
        verify(connection).close();
    }

    @Test
    @DisplayName("값 설정 람다로 수정 SQL의 매개변수를 바인딩한다")
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
    @DisplayName("익명 클래스의 값 설정 구현으로 수정 SQL을 실행한다")
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
    @DisplayName("조회용 연결을 얻지 못하면 원인을 보존한 데이터 접근 예외로 변환한다")
    void queryTranslatesConnectionFailure() throws SQLException {
        final var cause = new SQLException("연결 실패");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> "name"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verifyNoInteractions(connection, preparedStatement, resultSet);
    }

    @Test
    @DisplayName("수정용 Statement 준비에 실패하면 Connection을 닫는다")
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
    @DisplayName("조회 인자 바인딩에 실패하면 SQL을 실행하지 않고 자원을 닫는다")
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
    @DisplayName("수정 값 설정 중 SQL 예외가 발생하면 SQL을 실행하지 않고 자원을 닫는다")
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
    @DisplayName("조회 실행 중 SQL 예외가 발생하면 원인을 보존하고 자원을 닫는다")
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
    @DisplayName("수정 실행 중 SQL 예외가 발생하면 원인을 보존하고 자원을 닫는다")
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
    @DisplayName("결과 행 탐색에 실패하면 모든 조회 자원을 닫는다")
    void queryClosesAllResourcesWhenRowIterationFails() throws SQLException {
        final var cause = new SQLException("행 탐색 실패");
        when(resultSet.next()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> "name"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("행 매핑 중 SQL 예외가 발생하면 모든 조회 자원을 닫는다")
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
    @DisplayName("행 매핑 중 런타임 예외가 발생하면 그대로 전달하고 모든 자원을 닫는다")
    void queryPreservesUncheckedMapperExceptionAndClosesAllResources() throws SQLException {
        final var cause = new IllegalArgumentException("잘못된 상품");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> {
            throw cause;
        })).isSameAs(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("수정 값 설정 중 런타임 예외가 발생하면 그대로 전달하고 자원을 닫는다")
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
    @DisplayName("ResultSet 종료에 실패하면 원인을 보존하고 나머지 자원도 닫는다")
    void queryTranslatesResultSetCloseFailureAndClosesRemainingResources() throws SQLException {
        final var cause = new SQLException("결과 반환 실패");
        doThrow(cause).when(resultSet).close();

        assertThatThrownBy(() -> jdbcTemplate.query("select name from products", row -> "name"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("수정 후 Statement 종료에 실패해도 Connection을 닫고 원인을 보존한다")
    void updateTranslatesStatementCloseFailureAndClosesConnection() throws SQLException {
        final var cause = new SQLException("구문 반환 실패");
        doThrow(cause).when(preparedStatement).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from products"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
    }

    @Test
    @DisplayName("수정 후 Connection 종료에 실패하면 원인을 보존한 데이터 접근 예외로 변환한다")
    void updateTranslatesConnectionCloseFailure() throws SQLException {
        final var cause = new SQLException("연결 반환 실패");
        doThrow(cause).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("delete from products"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    @DisplayName("행 매핑과 자원 종료가 함께 실패하면 원래 예외에 종료 예외들을 추가한다")
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

    @Test
    @DisplayName("외부 Connection으로 조회하면 행을 매핑하고 조회 자원만 닫는다")
    void queryWithCallerConnectionMapsRowsAndClosesOnlyQueryResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("keyboard");

        final var rows = jdbcTemplate.query(connection, "select name from products where price > ?",
                row -> row.getString("name"), 1000);

        assertThat(rows).containsExactly("keyboard");
        verify(preparedStatement).setObject(1, 1000);
        final var closeOrder = inOrder(resultSet, preparedStatement);
        closeOrder.verify(resultSet).close();
        closeOrder.verify(preparedStatement).close();
        verifyCallerConnectionWasNotManaged();
    }

    @Test
    @DisplayName("외부 Connection으로 수정하면 인자를 바인딩하고 Statement만 닫는다")
    void updateWithCallerConnectionBindsParametersAndClosesStatement() throws SQLException {
        jdbcTemplate.update(connection, "insert into products (name, description) values (?, ?)", "keyboard", null);

        final var order = inOrder(preparedStatement);
        order.verify(preparedStatement).setObject(1, "keyboard");
        order.verify(preparedStatement).setObject(2, null);
        order.verify(preparedStatement).executeUpdate();
        order.verify(preparedStatement).close();
        verifyCallerConnectionWasNotManaged();
    }

    @Test
    @DisplayName("외부 Connection과 값 설정 람다로 조회한다")
    void queryWithCallerConnectionAcceptsSetterLambda() throws SQLException {
        jdbcTemplate.query(connection, "select name from products where price > ?",
                statement -> statement.setInt(1, 1000), row -> row.getString("name"));

        verify(preparedStatement).setInt(1, 1000);
        verify(resultSet).close();
        verify(preparedStatement).close();
        verifyCallerConnectionWasNotManaged();
    }

    @Test
    @DisplayName("외부 Connection과 값 설정 람다로 수정한다")
    void updateWithCallerConnectionAcceptsSetterLambda() throws SQLException {
        jdbcTemplate.update(connection, "update products set price = ?", statement -> statement.setInt(1, 1000));

        verify(preparedStatement).setInt(1, 1000);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
        verifyCallerConnectionWasNotManaged();
    }

    @Test
    @DisplayName("수정에 실패해도 외부 Connection을 닫거나 롤백하지 않는다")
    void updateFailureDoesNotCloseOrRollbackCallerConnection() throws SQLException {
        final var failure = new SQLException("수정 실패");
        when(preparedStatement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, "update products set price = ?", 1000))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(preparedStatement).close();
        verifyCallerConnectionWasNotManaged();
    }

    @Test
    @DisplayName("행 매핑에 실패하면 결과와 Statement만 닫고 외부 Connection은 유지한다")
    void mappingFailureClosesRowsAndStatementButKeepsCallerConnectionOpen() throws SQLException {
        final var failure = new SQLException("매핑 실패");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(connection, "select name from products", row -> {
            throw failure;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verifyCallerConnectionWasNotManaged();
    }

    @Test
    @DisplayName("Statement 준비에 실패해도 외부 Connection은 유지한다")
    void preparationFailureKeepsCallerConnectionOpen() throws SQLException {
        final var failure = new SQLException("준비 실패");
        when(connection.prepareStatement(anyString())).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, "invalid sql"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verifyNoInteractions(preparedStatement, resultSet);
        verifyCallerConnectionWasNotManaged();
    }

    private void verifyCallerConnectionWasNotManaged() throws SQLException {
        verifyNoInteractions(dataSource);
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection, never()).setAutoCommit(anyBoolean());
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
