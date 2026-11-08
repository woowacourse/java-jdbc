package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.unbindResource(dataSource);
    }

    @Test
    @DisplayName("가변 인자를 순서대로 바인딩하고 수정된 행 수를 반환한다")
    void updateBindsArgumentsInOrder() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        int affectedRows = jdbcTemplate.update(
                "update users set password = ? where id = ?", "password99", 1L);

        assertThat(affectedRows).isEqualTo(1);
        InOrder order = inOrder(statement);
        order.verify(statement).setObject(1, "password99");
        order.verify(statement).setObject(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("setter로 SQL 타입을 지정해 바인딩하고 수정된 행 수를 반환한다")
    void updateUsesCustomSetter() throws SQLException {
        Date date = Date.valueOf("2026-01-01");
        when(statement.executeUpdate()).thenReturn(2);

        int affectedRows = jdbcTemplate.update(
                "update events set description = ?, event_date = ?",
                pstmt -> {
                    pstmt.setNull(1, Types.VARCHAR);
                    pstmt.setDate(2, date);
                });

        assertThat(affectedRows).isEqualTo(2);
        InOrder order = inOrder(statement);
        order.verify(statement).setNull(1, Types.VARCHAR);
        order.verify(statement).setDate(2, date);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("트랜잭션에 등록된 Connection으로 SQL을 실행하고 Connection을 닫지 않는다")
    void updateWithBoundConnectionLeavesItOpen() throws SQLException {
        Connection boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);
        when(boundConnection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        int affectedRows = jdbcTemplate.update(
                "update users set password = ? where id = ?", "newPassword", 1L);

        assertThat(affectedRows).isEqualTo(1);
        verify(statement).setObject(1, "newPassword");
        verify(statement).setObject(2, 1L);
        verify(statement).close();
        verify(boundConnection, never()).close();
        verify(boundConnection, never()).commit();
        verify(boundConnection, never()).rollback();
        verify(boundConnection, never()).setAutoCommit(anyBoolean());
        verifyNoInteractions(dataSource, connection);
    }

    @Test
    @DisplayName("트랜잭션에 등록된 Connection에서도 Setter로 SQL 타입을 지정하고 연결을 유지한다")
    void updateWithBoundConnectionUsesCustomSetter() throws SQLException {
        Connection boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);
        String sql = "update events set description = ?, event_date = ?";
        Date date = Date.valueOf("2026-01-01");
        when(boundConnection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(2);

        int affectedRows = jdbcTemplate.update(sql, pstmt -> {
            pstmt.setNull(1, Types.VARCHAR);
            pstmt.setDate(2, date);
        });

        assertThat(affectedRows).isEqualTo(2);
        verify(boundConnection).prepareStatement(sql);
        InOrder order = inOrder(statement);
        order.verify(statement).setNull(1, Types.VARCHAR);
        order.verify(statement).setDate(2, date);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verify(boundConnection, never()).close();
        verify(boundConnection, never()).commit();
        verify(boundConnection, never()).rollback();
        verify(boundConnection, never()).setAutoCommit(anyBoolean());
        verifyNoInteractions(dataSource, connection);
    }

    @Test
    @DisplayName("트랜잭션에 등록된 Connection의 Setter 바인딩이 실패하면 Statement만 닫고 원인을 보존한다")
    void setterBindingFailureWithBoundConnectionClosesOnlyStatement() throws SQLException {
        Connection boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);
        SQLException cause = new SQLException("Setter 바인딩 실패");
        when(boundConnection.prepareStatement(anyString())).thenReturn(statement);
        doThrow(cause).when(statement).setNull(1, Types.VARCHAR);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "update events set description = ?",
                pstmt -> pstmt.setNull(1, Types.VARCHAR)))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(boundConnection, never()).close();
        verify(boundConnection, never()).commit();
        verify(boundConnection, never()).rollback();
        verify(boundConnection, never()).setAutoCommit(anyBoolean());
        verifyNoInteractions(dataSource, connection);
    }

    @Test
    @DisplayName("트랜잭션에 등록된 Connection에서 SQL이 실패해도 Connection을 닫지 않고 원인을 보존한다")
    void updateFailureWithBoundConnectionLeavesItOpen() throws SQLException {
        Connection boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);
        SQLException cause = new SQLException("SQL 실행 실패");
        when(boundConnection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "update users set password = ? where id = ?", "newPassword", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(boundConnection, never()).close();
        verify(boundConnection, never()).commit();
        verify(boundConnection, never()).rollback();
        verify(boundConnection, never()).setAutoCommit(anyBoolean());
        verifyNoInteractions(dataSource, connection);
    }

    @Test
    @DisplayName("트랜잭션에 등록된 Connection으로 단건 조회하고 조회 자원만 닫는다")
    void queryForObjectWithBoundConnectionLeavesItOpen() throws SQLException {
        Connection boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);
        ResultSet resultSet = mock(ResultSet.class);
        when(boundConnection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        String result = jdbcTemplate.queryForObject(
                "select account from users where id = ?",
                rs -> rs.getString("account"), 1L);

        assertThat(result).isEqualTo("gugu");
        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(boundConnection, never()).close();
        verify(boundConnection, never()).commit();
        verify(boundConnection, never()).rollback();
        verifyNoInteractions(connection);
    }

    @Test
    @DisplayName("트랜잭션에 등록된 Connection의 조회 결과 변환이 실패해도 조회 자원만 닫고 원인을 보존한다")
    void queryForObjectMappingFailureWithBoundConnectionLeavesItOpen() throws SQLException {
        Connection boundConnection = mock(Connection.class);
        TransactionSynchronizationManager.bindResource(dataSource, boundConnection);
        ResultSet resultSet = mock(ResultSet.class);
        SQLException cause = new SQLException("조회 결과 변환 실패");
        when(boundConnection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?",
                rs -> {
                    throw cause;
                }, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(boundConnection, never()).close();
        verify(boundConnection, never()).commit();
        verify(boundConnection, never()).rollback();
        verifyNoInteractions(connection);
    }

    @Test
    @DisplayName("Statement 준비가 실패하면 원인을 보존하고 Connection을 닫는다")
    void statementPreparationFailureClosesConnection() throws SQLException {
        SQLException cause = new SQLException("Statement 준비 실패");
        when(connection.prepareStatement(anyString())).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users where id = ?", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
        verifyNoInteractions(statement);
    }

    @Test
    @DisplayName("가변 인자 바인딩이 실패하면 실행하지 않고 원인을 보존하며 자원을 닫는다")
    void argumentBindingFailureClosesResources() throws SQLException {
        SQLException cause = new SQLException("파라미터 바인딩 실패");
        doThrow(cause).when(statement).setObject(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users where id = ?", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("setter 바인딩이 실패하면 실행하지 않고 원인을 보존하며 자원을 닫는다")
    void setterBindingFailureClosesResources() throws SQLException {
        SQLException cause = new SQLException("setter 바인딩 실패");
        doThrow(cause).when(statement).setLong(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "delete from users where id = ?", pstmt -> pstmt.setLong(1, 1L)))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
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
    @DisplayName("목록 조회 결과가 없으면 빈 리스트를 반환하고 자원을 닫는다")
    void queryReturnsEmptyListWhenNoRows() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<String> result = jdbcTemplate.query(
                "select account from users", rs -> rs.getString("account"));

        assertThat(result).isEmpty();
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("목록 조회 결과가 여러 건이면 모든 행을 순서대로 변환하고 자원을 닫는다")
    void queryReturnsAllMappedRowsInOrder() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "bobo");

        List<String> result = jdbcTemplate.query(
                "select account from users", rs -> rs.getString("account"));

        assertThat(result).containsExactly("gugu", "bobo");
        verify(resultSet).close();
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

    @Test
    @DisplayName("단건 조회 결과가 없으면 null을 반환한다")
    void queryForObjectReturnsNullWhenNoRows() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        String result = jdbcTemplate.queryForObject(
                "select account from users where account = ?",
                rs -> rs.getString("account"), "gugu");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("단건 조회 결과가 1건이면 변환한 객체를 반환한다")
    void queryForObjectReturnsMappedObjectWhenOneRow() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        String result = jdbcTemplate.queryForObject(
                "select account from users where account = ?",
                rs -> rs.getString("account"), "gugu");

        assertThat(result).isEqualTo("gugu");
    }

    @Test
    @DisplayName("단건 조회 결과가 2건 이상이면 예외를 던지고 자원을 닫는다")
    void queryForObjectRejectsMultipleRows() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where account = ?",
                rs -> rs.getString("account"), "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("단건 조회 결과는 최대 1건이어야 합니다.");

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
