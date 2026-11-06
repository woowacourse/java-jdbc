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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateQueryTest {

    private static final String SQL = "select account from users where id >= ?";

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;
    private PreparedStatementSetter setter;
    private RowMapper<String> rowMapper;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate() {
            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }
        };

        rowMapper = resultSet -> resultSet.getString("account");
        setter = statement -> statement.setLong(1, 1L);
    }

    @Test
    @DisplayName("값 설정 후 모든 행을 순서대로 변환하고 조회 자원을 닫는다")
    void queryMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        final List<String> results = jdbcTemplate.query(SQL, setter, rowMapper);

        assertThat(results).containsExactly("gugu", "pobi");
        final var order = inOrder(statement, resultSet, connection);
        order.verify(statement).setLong(1, 1L);
        order.verify(statement).executeQuery();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    @DisplayName("가변 인자로 조회 조건을 전달하고 캐스팅 없이 결과를 반환받는다")
    void queryWithParameters() throws SQLException {
        final var sql = "select account from users where id >= ? and account <> ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final List<String> results = jdbcTemplate.query(sql, rowMapper, 1L, "excluded");

        assertThat(results).containsExactly("gugu");
        verify(statement).setObject(1, 1L);
        verify(statement).setObject(2, "excluded");
        verify(statement).executeQuery();
    }

    @Test
    @DisplayName("조회 인자가 없으면 setter 없이 조회하고 결과를 변환한다")
    void queryWithoutSetter() throws SQLException {
        final var sql = "select account from users";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final var results = jdbcTemplate.query(sql, null, rowMapper);

        assertThat(results).isEqualTo(List.of("gugu"));
        verify(statement, never()).setLong(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("값 설정에 실패하면 조회를 실행하지 않고 자원을 닫는다")
    void queryFailsDuringBinding() throws SQLException {
        final var cause = new SQLException("값 설정 실패");
        doThrow(cause).when(statement).setLong(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, setter, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeQuery();
        verify(statement).close();
        verify(connection).close();
        verifyNoInteractions(resultSet);
    }

    @Test
    @DisplayName("조회 실행에 실패하면 원인을 보존하고 자원을 닫는다")
    void queryFailsDuringExecution() throws SQLException {
        final var cause = new SQLException("조회 실행 실패");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, setter, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
        verifyNoInteractions(resultSet);
    }

    @Test
    @DisplayName("결과 매핑에 실패해도 ResultSet과 나머지 자원을 모두 닫는다")
    void queryFailsDuringMapping() throws SQLException {
        final var cause = new SQLException("결과 매핑 실패");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, setter, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryWithBoundConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.query(SQL, rowMapper, 1L)).containsExactly("gugu");

        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verifyNoInteractions(dataSource);
    }

    @Test
    void boundConnectionStaysOpenWhenMappingFails() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var cause = new SQLException("매핑 실패");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, setter, rowMapper))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verify(connection, never()).rollback();
        verifyNoInteractions(dataSource);
    }
    @Test
    void queryForObjectThrowsWhenEmpty() throws SQLException {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("단건 조회 결과가 없습니다");

        verify(resultSet, never()).getString("account");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectThrowsWithoutMappingRemainingRowsWhenMultipleRowsExist() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("단건 조회 결과가 여러 건입니다");

        verify(resultSet, times(2)).next();
        verify(resultSet).getString("account");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObjectUsesBoundConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.queryForObject(SQL, rowMapper, 1L)).isEqualTo("gugu");

        verify(statement).setObject(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verifyNoInteractions(dataSource);
    }

    @Test
    void queryForObjectWithoutResultsKeepsBoundConnectionOpen() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection, never()).close();
        verifyNoInteractions(dataSource);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    void queryForObjectFailsDuringBinding() throws SQLException {
        final var cause = new SQLException("단건 조회 인자 바인딩 실패");
        doThrow(cause).when(statement).setObject(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement, never()).executeQuery();
        verify(statement).close();
        verify(connection).close();
        verifyNoInteractions(resultSet);
    }

    @Test
    void queryForObjectFailsDuringMapping() throws SQLException {
        final var cause = new SQLException("단건 매핑 실패");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rowMapper, 1L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
