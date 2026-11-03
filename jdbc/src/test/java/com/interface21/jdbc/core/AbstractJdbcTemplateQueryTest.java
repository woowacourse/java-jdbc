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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractJdbcTemplateQueryTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final String sql = "select account from users where id = ?";
    private final AbstractJdbcTemplate jdbcTemplate = new AbstractJdbcTemplate() {
        @Override
        protected String createQuery() {
            return sql;
        }

        @Override
        protected DataSource getDataSource() {
            return dataSource;
        }

        @Override
        protected void setValues(final PreparedStatement statement) throws SQLException {
            statement.setLong(1, 1L);
        }

        @Override
        protected Object mapRow(final ResultSet resultSet) throws SQLException {
            return resultSet.next() ? resultSet.getString("account") : null;
        }
    };

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    @DisplayName("매핑한 결과를 반환하고 모든 자원을 해제한다")
    void query() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.query()).isEqualTo("gugu");

        verify(statement).setLong(1, 1L);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 실행이 실패하면 SQL과 원인을 보존하고 자원을 해제한다")
    void query_failure() throws SQLException {
        final var failure = new SQLException("조회 실행 실패");
        when(statement.executeQuery()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query())
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("매핑이 실패하면 SQL과 원인을 보존하고 모든 자원을 해제한다")
    void mapping_failure() throws SQLException {
        final var failure = new SQLException("결과 매핑 실패");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query())
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("매핑 중 런타임 예외가 발생해도 같은 예외를 던지고 모든 자원을 해제한다")
    void mapping_runtime_failure() throws SQLException {
        final var failure = new IllegalArgumentException("결과 변환 실패");
        when(resultSet.next()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query()).isSameAs(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
