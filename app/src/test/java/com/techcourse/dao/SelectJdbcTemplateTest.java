package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SelectJdbcTemplateTest {

    private static final String SQL = "select account from users where id >= ?";

    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private SelectJdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new SelectJdbcTemplate() {
            @Override
            protected String createQuery() {
                return SQL;
            }

            @Override
            protected DataSource getDataSource() {
                return dataSource;
            }

            @Override
            protected Object mapRow(final ResultSet resultSet) throws SQLException {
                return resultSet.getString("account");
            }

            @Override
            protected void setValues(final PreparedStatement statement) throws SQLException {
                statement.setLong(1, 1L);
            }
        };
    }

    @Test
    @DisplayName("값 설정 후 모든 행을 순서대로 변환하고 조회 자원을 닫는다")
    void queryMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "pobi");

        final var results = jdbcTemplate.query();

        assertThat(results).isEqualTo(List.of("gugu", "pobi"));
        final var order = inOrder(statement, resultSet, connection);
        order.verify(statement).setLong(1, 1L);
        order.verify(statement).executeQuery();
        order.verify(resultSet).close();
        order.verify(statement).close();
        order.verify(connection).close();
    }

    @Test
    @DisplayName("값 설정에 실패하면 조회를 실행하지 않고 자원을 닫는다")
    void queryFailsDuringBinding() throws SQLException {
        final var cause = new SQLException("값 설정 실패");
        doThrow(cause).when(statement).setLong(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.query())
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

        assertThatThrownBy(() -> jdbcTemplate.query())
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

        assertThatThrownBy(() -> jdbcTemplate.query())
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
