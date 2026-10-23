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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    @DisplayName("변경 쿼리는 인자를 순서대로 바인딩하고 실행한 뒤 자원을 닫는다")
    void commandBindsParametersAndExecutesUpdate() throws SQLException {
        final var sql = "update users set account = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        jdbcTemplate.command(sql, pstmt -> {
            pstmt.setString(1, "gugu");
            pstmt.setLong(2, 1L);
        });

        verify(statement).setString(1, "gugu");
        verify(statement).setLong(2, 1L);
        verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("setter는 null을 포함해 순서대로 바인딩한 뒤 변경 쿼리를 실행한다")
    void commandBindsParametersIncludingNullInOrder() throws SQLException {
        final var sql = "update users set account = ?, email = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        jdbcTemplate.command(sql, pstmt -> {
            pstmt.setString(1, "gugu");
            pstmt.setNull(2, Types.VARCHAR);
            pstmt.setLong(3, 1L);
        });

        final var order = inOrder(statement);
        order.verify(statement).setString(1, "gugu");
        order.verify(statement).setNull(2, Types.VARCHAR);
        order.verify(statement).setLong(3, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("setter로 null 하나를 변경 쿼리에 바인딩한다")
    void commandBindsSingleNullWithSetter() throws SQLException {
        final var sql = "update users set email = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        jdbcTemplate.command(sql, pstmt -> pstmt.setNull(1, Types.VARCHAR));

        verify(statement).setNull(1, Types.VARCHAR);
        verify(statement).executeUpdate();
    }

    @Test
    @DisplayName("setter로 null 하나를 목록 조회에 바인딩한다")
    void queryBindsSingleNullWithSetter() throws SQLException {
        final var sql = "select account from users where coalesce(email, '') = coalesce(?, '')";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var results = jdbcTemplate.query(sql, rs -> rs.getString("account"),
                pstmt -> pstmt.setNull(1, Types.VARCHAR));

        assertThat(results).containsExactly("gugu");
        verify(statement).setNull(1, Types.VARCHAR);
    }

    @Test
    @DisplayName("setter로 null 하나를 단건 조회에 바인딩한다")
    void queryForObjectBindsSingleNullWithSetter() throws SQLException {
        final var sql = "select account from users where coalesce(email, '') = coalesce(?, '')";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var result = jdbcTemplate.queryForObject(sql, rs -> rs.getString("account"),
                pstmt -> pstmt.setNull(1, Types.VARCHAR));

        assertThat(result).contains("gugu");
        verify(statement).setNull(1, Types.VARCHAR);
    }

    @Test
    @DisplayName("파라미터가 없어도 변경 쿼리를 실행한다")
    void commandExecutesWithoutParameters() throws SQLException {
        final var sql = "delete from users";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        jdbcTemplate.command(sql);

        verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("setter로 목록 조회 조건을 바인딩하고 결과를 변환한다")
    void queryBindsWithSetterAndMapsRows() throws SQLException {
        final var sql = "select account from users where id > ?";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "isaac");

        final var results = jdbcTemplate.query(sql, rs -> rs.getString("account"),
                pstmt -> pstmt.setLong(1, 0L));

        assertThat(results).containsExactly("gugu", "isaac");
        verify(statement).setLong(1, 0L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("setter로 단건 조회 조건을 바인딩하고 객체를 반환한다")
    void queryForObjectBindsWithSetterAndReturnsMappedObject() throws SQLException {
        final var sql = "select account from users where id = ?";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var result = jdbcTemplate.queryForObject(sql, rs -> rs.getString("account"),
                pstmt -> pstmt.setLong(1, 1L));

        assertThat(result).contains("gugu");
        verify(statement).setLong(1, 1L);
    }

    @Test
    @DisplayName("목록 조회는 인자를 바인딩하고 모든 행을 변환한 뒤 자원을 닫는다")
    void queryMapsEveryRowAndBindsParameters() throws SQLException {
        final var sql = "select account from users where id > ?";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "isaac");

        final var results = jdbcTemplate.query(sql, rs -> rs.getString("account"),
                pstmt -> pstmt.setLong(1, 0L));

        assertThat(results).containsExactly("gugu", "isaac");
        verify(statement).setLong(1, 0L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
    void queryReturnsEmptyListWhenNoRowsExist() throws SQLException {
        final var sql = "select account from users";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(false);

        assertThat(jdbcTemplate.query(sql, rs -> rs.getString("account"))).isEmpty();
    }

    @Test
    @DisplayName("단건 조회 결과가 한 행이면 변환된 객체를 담은 Optional을 반환한다")
    void queryForObjectReturnsSingleMappedObject() throws SQLException {
        final var sql = "select account from users where id = ?";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var result = jdbcTemplate.queryForObject(sql, rs -> rs.getString("account"),
                pstmt -> pstmt.setLong(1, 1L));

        assertThat(result).contains("gugu");
        verify(statement).setLong(1, 1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 빈 Optional을 반환한다")
    void queryForObjectReturnsEmptyWhenNoRowsExist() throws SQLException {
        final var sql = "select account from users where id = ?";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(false);

        final var result = jdbcTemplate.queryForObject(sql, rs -> rs.getString("account"),
                pstmt -> pstmt.setLong(1, 1L));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("단건 조회 결과가 여러 행이면 예외가 발생한다")
    void queryForObjectRejectsMultipleRows() throws SQLException {
        final var sql = "select account from users";
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "isaac");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rs -> rs.getString("account")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("변경 쿼리 실행이 실패하면 원인 예외를 보존하고 자원을 닫는다")
    void commandPreservesSqlExceptionAndClosesResources() throws SQLException {
        final var sql = "insert into users (account) values (?)";
        final var failure = new SQLException("execution failed");
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.command(sql, pstmt -> pstmt.setString(1, "gugu")))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다.")
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("변경 쿼리 실행과 자원 종료가 모두 실패하면 실행 예외에 종료 예외를 보존한다")
    void commandPreservesExecutionFailureAndSuppressedCloseFailures() throws SQLException {
        final var sql = "insert into users (account) values (?)";
        final var executionFailure = new SQLException("execution failed");
        final var statementCloseFailure = new SQLException("statement close failed");
        final var connectionCloseFailure = new SQLException("connection close failed");
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(executionFailure);
        doThrow(statementCloseFailure).when(statement).close();
        doThrow(connectionCloseFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.command(sql, pstmt -> pstmt.setString(1, "gugu")))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다.")
                .hasCause(executionFailure);
        assertThat(executionFailure.getSuppressed())
                .containsExactly(statementCloseFailure, connectionCloseFailure);

        final var order = inOrder(statement, connection);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    @DisplayName("조회 중 행 읽기와 모든 자원 종료가 실패하면 읽기 예외에 종료 예외를 보존한다")
    void queryPreservesReadFailureAndSuppressedCloseFailures() throws SQLException {
        final var sql = "select account from users";
        final var readFailure = new SQLException("row read failed");
        final var resultSetCloseFailure = new SQLException("result set close failed");
        final var statementCloseFailure = new SQLException("statement close failed");
        final var connectionCloseFailure = new SQLException("connection close failed");
        prepareQuery(sql);
        when(resultSet.next()).thenThrow(readFailure);
        doThrow(resultSetCloseFailure).when(resultSet).close();
        doThrow(statementCloseFailure).when(statement).close();
        doThrow(connectionCloseFailure).when(connection).close();

        assertThatThrownBy(() -> jdbcTemplate.query(sql, rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다.")
                .hasCause(readFailure);
        assertThat(readFailure.getSuppressed())
                .containsExactly(resultSetCloseFailure, statementCloseFailure, connectionCloseFailure);

        final var order = inOrder(resultSet, statement, connection);
        order.verify(resultSet).next();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    @DisplayName("행 변환이 실패하면 원인 예외를 보존하고 조회 자원을 모두 닫는다")
    void queryClosesResourcesWhenMappingFails() throws SQLException {
        final var sql = "select account from users";
        final var failure = new SQLException("mapping failed");
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(sql, rs -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다.")
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과를 닫다가 실패하면 원인 예외를 보존하고 나머지 자원을 닫는다")
    void queryStillClosesOtherResourcesWhenResultSetCloseFails() throws SQLException {
        final var sql = "select account from users";
        final var failure = new SQLException("close failed");
        prepareQuery(sql);
        when(resultSet.next()).thenReturn(false);
        doThrow(failure).when(resultSet).close();

        assertThatThrownBy(() -> jdbcTemplate.query(sql, rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("SQL 실행에 실패했습니다.")
                .hasCause(failure);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    private void prepareQuery(final String sql) throws SQLException {
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }
}
