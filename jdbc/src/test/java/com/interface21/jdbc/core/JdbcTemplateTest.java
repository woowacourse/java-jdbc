package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("기존 setter로 타입을 지정해 바인딩하고 영향받은 행 수를 반환한다")
    void updateBindsTypedParametersBeforeExecutionAndReturnsAffectedRows() throws SQLException {
        final var sql = "update sample set name = ?, age = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(2);

        final int actual = jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setString(1, "Alice");
            preparedStatement.setNull(2, Types.INTEGER);
            preparedStatement.setLong(3, 10L);
        });

        assertThat(actual).isEqualTo(2);
        verify(connection).prepareStatement(sql);
        final var order = inOrder(statement, connection);
        order.verify(statement).setString(1, "Alice");
        order.verify(statement).setNull(2, Types.INTEGER);
        order.verify(statement).setLong(3, 10L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    @DisplayName("조회 행을 순서대로 매핑하고 ResultSet·Statement·연결 순서로 닫는다")
    void queryMapsEveryRowInOrderAndClosesResourcesAfterMapping() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("Alice", "Bob");

        final var actual = jdbcTemplate.query("select name from sample", ignored -> {},
                row -> row.getString("name"));

        assertThat(actual).containsExactly("Alice", "Bob");
        final var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).next();
        order.verify(resultSet).getString("name");
        order.verify(resultSet).next();
        order.verify(resultSet).getString("name");
        order.verify(resultSet).next();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    @DisplayName("목록 조회 결과가 없으면 매퍼를 호출하지 않고 빈 목록을 반환한다")
    void queryReturnsEmptyListWithoutMappingWhenNoRowsExist() throws SQLException {
        final var actual = jdbcTemplate.query("select name from sample", ignored -> {},
                row -> row.getString("name"));

        assertThat(actual).isEmpty();
        verify(resultSet, never()).getString("name");
        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 매퍼를 호출하지 않고 null을 반환한다")
    void queryForObjectReturnsNullWithoutMappingWhenNoRowsExist() throws SQLException {
        final var actual = jdbcTemplate.queryForObject("select id from sample", ignored -> {},
                row -> row.getLong("id"));

        assertThat(actual).isNull();
        verify(resultSet, never()).getLong("id");
        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("단건 조회는 여러 행이 있어도 첫 번째 행만 읽고 반환한다")
    void queryForObjectReturnsOnlyTheFirstRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(10L, 20L);

        final var actual = jdbcTemplate.queryForObject("select id from sample order by id", ignored -> {},
                row -> row.getLong("id"));

        assertThat(actual).isEqualTo(10L);
        verify(resultSet, times(1)).next();
        verify(resultSet, times(1)).getLong("id");
        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("연결 획득 실패 시 원인을 보존하고 획득하지 않은 자원에 접근하지 않는다")
    void acquisitionFailurePreservesTheSqlCauseWithoutClosingUnacquiredResources() throws SQLException {
        final var failure = new SQLException("connection unavailable");
        when(dataSource.getConnection()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("update sample set name = 'Alice'", ignored -> {}))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(connection, statement, resultSet);
    }

    @Test
    @DisplayName("Statement 생성 실패 시 획득한 연결만 닫는다")
    void preparationFailureClosesOnlyTheAcquiredConnection() throws SQLException {
        final var failure = new SQLException("cannot prepare");
        when(connection.prepareStatement(anyString())).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from sample", ignored -> {},
                row -> row.getString("name")))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(connection).close();
        verifyNoInteractions(statement, resultSet);
    }

    @Test
    @DisplayName("수정 setter의 SQL 예외는 원인을 보존하고 SQL 실행 전에 자원을 닫는다")
    void updateSetterFailurePreventsExecutionAndClosesResources() throws SQLException {
        final var failure = new SQLException("cannot bind update");

        assertThatThrownBy(() -> jdbcTemplate.update("update sample set name = ?", preparedStatement -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(statement, never()).executeUpdate();
        verifyStatementAndConnectionClosed();
        verifyNoInteractions(resultSet);
    }

    @Test
    @DisplayName("목록 조회 setter의 SQL 예외는 원인을 보존하고 결과를 생성하지 않는다")
    void querySetterFailurePreventsExecutionAndClosesResources() throws SQLException {
        final var failure = new SQLException("cannot bind query");

        assertThatThrownBy(() -> jdbcTemplate.query("select name from sample where id = ?", preparedStatement -> {
            throw failure;
        }, row -> row.getString("name")))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(statement, never()).executeQuery();
        verifyStatementAndConnectionClosed();
        verifyNoInteractions(resultSet);
    }

    @Test
    @DisplayName("단건 조회 setter의 SQL 예외는 원인을 보존하고 결과를 생성하지 않는다")
    void queryForObjectSetterFailurePreventsExecutionAndClosesResources() throws SQLException {
        final var failure = new SQLException("cannot bind single query");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from sample where id = ?",
                preparedStatement -> { throw failure; }, row -> row.getString("name")))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(statement, never()).executeQuery();
        verifyStatementAndConnectionClosed();
        verifyNoInteractions(resultSet);
    }

    @Test
    @DisplayName("수정 SQL 실패 시 원인을 보존하고 Statement와 연결을 닫는다")
    void updateExecutionFailurePreservesTheSqlCauseAndClosesResources() throws SQLException {
        final var failure = new SQLException("update rejected");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update("update sample set name = ?", ignored -> {}))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyStatementAndConnectionClosed();
    }

    @Test
    @DisplayName("목록 조회 SQL 실패 시 아직 얻지 않은 ResultSet은 닫지 않는다")
    void queryExecutionFailureClosesOnlyStatementAndConnection() throws SQLException {
        final var failure = new SQLException("query rejected");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from sample", ignored -> {},
                row -> row.getString("name")))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(resultSet);
        verifyStatementAndConnectionClosed();
    }

    @Test
    @DisplayName("단건 조회 SQL 실패 시 아직 얻지 않은 ResultSet은 닫지 않는다")
    void queryForObjectExecutionFailureClosesOnlyStatementAndConnection() throws SQLException {
        final var failure = new SQLException("single query rejected");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from sample", ignored -> {},
                row -> row.getString("name")))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyNoInteractions(resultSet);
        verifyStatementAndConnectionClosed();
    }

    @Test
    @DisplayName("커서 이동 실패 시 원인을 보존하고 조회 자원을 모두 닫는다")
    void cursorFailurePreservesTheSqlCauseAndClosesAllResources() throws SQLException {
        final var failure = new SQLException("cannot read next row");
        when(resultSet.next()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from sample", ignored -> {},
                row -> row.getString("name")))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("목록 매퍼의 SQL 예외는 원인을 보존하고 조회 자원을 모두 닫는다")
    void queryMapperSqlFailurePreservesTheCauseAndClosesAllResources() throws SQLException {
        final var failure = new SQLException("cannot map row");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from sample", ignored -> {}, row -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("단건 매퍼의 SQL 예외는 원인을 보존하고 조회 자원을 모두 닫는다")
    void queryForObjectMapperSqlFailurePreservesTheCauseAndClosesAllResources() throws SQLException {
        final var failure = new SQLException("cannot map single row");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from sample", ignored -> {}, row -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("매핑 중 런타임 예외를 바꾸지 않고 자원을 닫은 뒤 전달한다")
    void mappingRuntimeFailurePropagatesUnchangedAfterCleanup() throws SQLException {
        final var failure = new IllegalStateException("invalid mapped value");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from sample", ignored -> {}, row -> {
            throw failure;
        })).isSameAs(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("ResultSet 종료 실패도 변환해 알리고 나머지 자원을 닫는다")
    void resultSetCloseFailureStillClosesStatementAndConnection() throws SQLException {
        final var failure = new SQLException("cannot close result set");
        doThrow(failure).when(resultSet).close();

        assertThatThrownBy(() -> jdbcTemplate.query("select name from sample", ignored -> {},
                row -> row.getString("name")))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("Statement 종료 실패도 변환해 알리고 연결을 닫는다")
    void statementCloseFailureStillClosesConnection() throws SQLException {
        final var failure = new SQLException("cannot close statement");
        doThrow(failure).when(statement).close();

        assertThatThrownBy(() -> jdbcTemplate.update("update sample set name = 'Alice'", ignored -> {}))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verifyStatementAndConnectionClosed();
    }

    @Test
    @DisplayName("실행 성공 뒤 연결 종료 실패도 무시하지 않고 원인을 보존한다")
    void connectionCloseFailureAfterExecutionIsReported() throws SQLException {
        final var failure = new SQLException("cannot close connection");
        doThrow(failure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.update("update sample set name = 'Alice'", ignored -> {}))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        verify(statement).executeUpdate();
        verifyStatementAndConnectionClosed();
    }

    @Test
    @DisplayName("정리 중 추가 실패 세 개를 원래 SQL 예외의 suppressed로 보존한다")
    void cleanupFailuresNeverReplaceTheOriginalSqlFailure() throws SQLException {
        final var failure = new SQLException("cannot read next row");
        final var resultCloseFailure = new SQLException("cannot close result set");
        final var statementCloseFailure = new SQLException("cannot close statement");
        final var connectionCloseFailure = new SQLException("cannot close connection");
        when(resultSet.next()).thenThrow(failure);
        doThrow(resultCloseFailure).when(resultSet).close();
        doThrow(statementCloseFailure).when(statement).close();
        doThrow(connectionCloseFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.query("select name from sample", ignored -> {},
                row -> row.getString("name")))
                .isInstanceOf(DataAccessException.class).hasCause(failure);

        assertThat(failure.getSuppressed())
                .containsExactly(resultCloseFailure, statementCloseFailure, connectionCloseFailure);
        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("수정 가변 인자는 SQL NULL을 포함해 물음표 순서대로 실행 전에 바인딩한다")
    void updateVarargsBindValuesInPlaceholderOrder() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final int actual = jdbcTemplate.update("update sample set name = ?, age = ? where id = ?",
                "Alice", null, 10L);

        assertThat(actual).isEqualTo(1);
        final var order = inOrder(statement);
        order.verify(statement).setObject(1, "Alice");
        order.verify(statement).setObject(2, null);
        order.verify(statement).setObject(3, 10L);
        order.verify(statement).executeUpdate();
    }

    @Test
    @DisplayName("목록 조회도 가변 인자를 먼저 바인딩하고 결과를 매핑한다")
    void queryVarargsBindBeforeExecutingAndMapping() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("Alice");

        final var actual = jdbcTemplate.query("select name from sample where id = ? and age = ?",
                row -> row.getString("name"), 10L, 20);

        assertThat(actual).containsExactly("Alice");
        final var order = inOrder(statement, resultSet);
        order.verify(statement).setObject(1, 10L);
        order.verify(statement).setObject(2, 20);
        order.verify(statement).executeQuery();
        order.verify(resultSet).next();
        order.verify(resultSet).getString("name");
        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("단건 조회도 가변 인자를 먼저 바인딩하고 결과를 매핑한다")
    void queryForObjectVarargsBindBeforeExecutingAndMapping() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("name")).thenReturn("Alice");

        final var actual = jdbcTemplate.queryForObject("select name from sample where id = ? and age = ?",
                row -> row.getString("name"), 10L, 20);

        assertThat(actual).isEqualTo("Alice");
        final var order = inOrder(statement, resultSet);
        order.verify(statement).setObject(1, 10L);
        order.verify(statement).setObject(2, 20);
        order.verify(statement).executeQuery();
        order.verify(resultSet).next();
        order.verify(resultSet).getString("name");
        verifyQueryResourcesClosed();
    }

    @Test
    @DisplayName("세 API 모두 인자가 없으면 바인딩 없이 실행한다")
    void noParametersSkipBindingForAllApis() throws SQLException {
        jdbcTemplate.update("delete from sample");
        final var rows = jdbcTemplate.query("select name from sample", row -> row.getString("name"));
        final var row = jdbcTemplate.queryForObject("select name from sample", result -> result.getString("name"));

        assertThat(rows).isEmpty();
        assertThat(row).isNull();
        verify(statement, never()).setObject(anyInt(), any());
        verify(statement).executeUpdate();
        verify(statement, times(2)).executeQuery();
        verify(resultSet, times(2)).close();
        verify(statement, times(3)).close();
        verify(connection, times(3)).close();
    }

    @Test
    @DisplayName("SQL NULL 하나는 Object null로 전달할 수 있다")
    void oneSqlNullIsOneParameterRatherThanANullArray() throws SQLException {
        jdbcTemplate.update("update sample set name = ?", (Object) null);

        verify(statement).setObject(1, null);
        verify(statement).executeUpdate();
        verifyStatementAndConnectionClosed();
    }

    @Test
    @DisplayName("null 가변 인자 배열은 세 API 모두 DB 연결 전에 거부한다")
    void nullParameterArraysAreRejectedBeforeOpeningAConnection() {
        final RowMapper<String> mapper = row -> row.getString("name");

        assertThatThrownBy(() -> jdbcTemplate.update("update sample set name = ?", (Object[]) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.query("select name from sample", mapper, (Object[]) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from sample", mapper, (Object[]) null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(dataSource, connection, statement, resultSet);
    }

    @Test
    @DisplayName("null SQL은 setter·가변 인자 API 모두 DB 연결 전에 거부한다")
    void nullSqlIsRejectedBeforeOpeningAConnection() {
        final PreparedStatementSetter setter = ignored -> {};
        final RowMapper<String> mapper = row -> row.getString("name");

        assertThatThrownBy(() -> jdbcTemplate.update(null, setter)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.query(null, setter, mapper)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(null, setter, mapper))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.query(null, mapper)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(null, mapper)).isInstanceOf(NullPointerException.class);

        verifyNoInteractions(dataSource, connection, statement, resultSet);
    }

    @Test
    @DisplayName("null setter는 세 API 모두 DB 연결 전에 거부한다")
    void nullSetterIsRejectedBeforeOpeningAConnection() {
        final var sql = "select name from sample";
        final RowMapper<String> mapper = row -> row.getString("name");

        assertThatThrownBy(() -> jdbcTemplate.update(sql, (PreparedStatementSetter) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.query(sql, (PreparedStatementSetter) null, mapper))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, (PreparedStatementSetter) null, mapper))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(dataSource, connection, statement, resultSet);
    }

    @Test
    @DisplayName("null mapper는 두 조회 API의 setter·가변 인자 방식 모두 DB 연결 전에 거부한다")
    void nullMapperIsRejectedBeforeOpeningAConnection() {
        final var sql = "select name from sample";
        final PreparedStatementSetter setter = ignored -> {};

        assertThatThrownBy(() -> jdbcTemplate.query(sql, setter, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, setter, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.query(sql, (RowMapper<String>) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, (RowMapper<String>) null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(dataSource, connection, statement, resultSet);
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        final var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    private void verifyStatementAndConnectionClosed() throws SQLException {
        final var order = inOrder(statement, connection);
        order.verify(statement).close();
        order.verify(connection).close();
    }
}
