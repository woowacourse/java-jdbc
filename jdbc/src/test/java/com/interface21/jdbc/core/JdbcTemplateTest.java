package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.RowMapper;
import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class JdbcTemplateTest {

    private static final String SELECT_SQL = "select account from users";
    private static final String UPDATE_SQL = "update users set account = ?, email = ? where id = ?";
    private static final RowMapper<String> accountRowMapper = (rs, row) -> rs.getString("account");

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    @DisplayName("인자를 순서대로 바인딩하고 변경 쿼리를 실행한 뒤 자원을 닫는다")
    void updateBindsArgumentsAndClosesResources() throws SQLException {
        jdbcTemplate.update(UPDATE_SQL, "gugu", "gugu@example.com", 1L);

        verify(connection).prepareStatement(UPDATE_SQL);
        InOrder calls = inOrder(statement);
        calls.verify(statement).setObject(1, "gugu");
        calls.verify(statement).setObject(2, "gugu@example.com");
        calls.verify(statement).setObject(3, 1L);
        calls.verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 인자를 순서대로 바인딩한 뒤 쿼리를 실행한다")
    void queryBindsArgumentsBeforeExecution() throws SQLException {
        final var sql = "select account from users where account = ? and id = ?";

        jdbcTemplate.query(sql, accountRowMapper, "gugu", 1L);

        verify(connection).prepareStatement(sql);
        InOrder calls = inOrder(statement);
        calls.verify(statement).setObject(1, "gugu");
        calls.verify(statement).setObject(2, 1L);
        calls.verify(statement).executeQuery();
    }

    @Test
    @DisplayName("조회된 모든 행을 순서대로 매핑해 반환하고 자원을 닫는다")
    void queryMapsAllRowsAndClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        final var accounts = jdbcTemplate.query(SELECT_SQL, accountRowMapper);

        assertThat(accounts).containsExactly("gugu", "pobi");
        verify(connection).prepareStatement(SELECT_SQL);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과가 없으면 매핑을 수행하지 않고 빈 목록을 반환한다")
    void queryReturnsEmptyListWhenNoRows() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final var accounts = jdbcTemplate.query(SELECT_SQL, accountRowMapper);

        assertThat(accounts).isEmpty();
        verify(resultSet, never()).getString("account");
    }

    @Test
    @DisplayName("단건 조회는 인자를 바인딩하고 매핑한 객체를 반환한다")
    void queryObjectReturnsMappedRow() throws SQLException {
        final var sql = "select account from users where id = ?";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var account = jdbcTemplate.queryObject(sql, accountRowMapper, 1L);

        assertThat(account).isEqualTo("gugu");
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 DataAccessException을 던지고 자원을 닫는다")
    void queryObjectThrowsWhenNoRows() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryObject(SELECT_SQL, accountRowMapper))
                .isInstanceOf(DataAccessException.class);

        verify(resultSet, never()).getString("account");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("연결 획득 중 SQLException이 발생하면 원인을 보존한 DataAccessException을 던진다")
    void updateWrapsConnectionSQLException() throws SQLException {
        final var cause = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() ->
                jdbcTemplate.update(UPDATE_SQL, "gugu", "gugu@example.com", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }

    @Test
    @DisplayName("조회 실행 중 SQLException이 발생하면 예외를 변환하고 열린 자원을 닫는다")
    void queryWrapsSQLExceptionAndClosesResources() throws SQLException {
        final var cause = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, accountRowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("변경 실행 중 SQLException이 발생하면 예외를 변환하고 열린 자원을 닫는다")
    void updateWrapsSQLExceptionAndClosesResources() throws SQLException {
        final var cause = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() ->
                jdbcTemplate.update(UPDATE_SQL, "gugu", "gugu@example.com", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("행 매핑 중 SQLException이 발생하면 예외를 변환하고 모든 자원을 닫는다")
    void queryClosesResourcesWhenMappingFails() throws SQLException {
        final var cause = new SQLException("mapping failed");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, (rs, row) -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
