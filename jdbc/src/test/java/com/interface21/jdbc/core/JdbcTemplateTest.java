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

import static org.assertj.core.api.Assertions.assertThat;
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
    private static final String SELECT_SQL = "select account from users";

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
        when(statement.executeQuery()).thenReturn(resultSet);
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

    @Test
    @DisplayName("조회 인자를 바인딩하고 각 행을 순서대로 변환한 뒤 자원을 닫는다")
    void query() throws SQLException {
        final var sql = SELECT_SQL + " where id >= ? and account <> ?";
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        final var results = jdbcTemplate.query(sql, rs -> rs.getString("account"), 1L, "excluded");

        assertThat(results).containsExactly("gugu", "pobi");
        verify(connection).prepareStatement(sql);
        final var order = inOrder(statement, resultSet, connection);
        order.verify(statement).setObject(1, 1L);
        order.verify(statement).setObject(2, "excluded");
        order.verify(statement).executeQuery();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과가 없으면 행을 변환하지 않고 빈 리스트를 반환한다")
    void queryWithoutResults() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final var results = jdbcTemplate.query(SELECT_SQL, rs -> rs.getString("account"));

        assertThat(results).isEmpty();
        verify(statement, never()).setObject(anyInt(), any());
        verify(resultSet, never()).getString(anyString());
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 실행에 실패하면 원인을 보존한 예외를 던지고 자원을 닫는다")
    void queryFailsDuringExecution() throws SQLException {
        final var cause = new SQLException("조회 실행 실패");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
        verifyNoInteractions(resultSet);
    }

    @Test
    @DisplayName("행 변환 중 SQL 예외가 발생해도 조회 자원을 모두 닫는다")
    void queryFailsDuringRowMapping() throws SQLException {
        final var cause = new SQLException("행 변환 실패");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("행 변환 중 런타임 예외가 발생하면 그대로 전달하고 자원을 닫는다")
    void queryFailsWithRuntimeExceptionDuringRowMapping() throws SQLException {
        final var cause = new IllegalArgumentException("잘못된 조회 데이터");
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, rs -> {
            throw cause;
        })).isSameAs(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회 결과가 한 건이면 변환한 객체를 반환한다")
    void queryForObject() throws SQLException {
        final var sql = SELECT_SQL + " where id = ?";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var result = jdbcTemplate.queryForObject(sql, rs -> rs.getString("account"), 1L);

        assertThat(result).isEqualTo("gugu");
        verify(statement).setObject(1, 1L);
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 null을 반환한다")
    void queryForObjectWithoutResults() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final var result = jdbcTemplate.queryForObject(SELECT_SQL, rs -> rs.getString("account"));

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("단건 조회 결과가 여러 건이면 예외를 던진다")
    void queryForObjectWithMultipleResults() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SELECT_SQL, rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("단건 조회 결과가 여러 건입니다");

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
