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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
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
    };

    private final PreparedStatementSetter setter = new PreparedStatementSetter() {
        @Override
        public void setValues(final PreparedStatement statement) throws SQLException {
            statement.setLong(1, 1L);
        }
    };
    private final RowMapper<String> rowMapper = new RowMapper<>() {
        @Override
        public String mapRow(final ResultSet resultSet) throws SQLException {
            return resultSet.getString("account");
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
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThat(jdbcTemplate.query(setter, rowMapper)).isEqualTo(List.of("gugu"));

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

        assertThatThrownBy(() -> jdbcTemplate.query(setter, rowMapper))
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
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.query(setter, rowMapper))
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

        assertThatThrownBy(() -> jdbcTemplate.query(setter, rowMapper)).isSameAs(failure);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 목록을 반환한다")
    void query_with_no_rows() {
        assertThat(jdbcTemplate.query(rowMapper)).isEqualTo(List.of());
    }

    @Test
    @DisplayName("조회한 여러 행을 순서대로 매핑한다")
    void query_with_multiple_rows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "brown");

        assertThat(jdbcTemplate.query(rowMapper)).isEqualTo(List.of("gugu", "brown"));
    }

    @Test
    @DisplayName("조회 값을 설정하다 실패하면 SQL과 원인을 보존하고 자원을 해제한다")
    void setter_failure() throws SQLException {
        final var failure = new SQLException("조회 값 설정 실패");
        doThrow(failure).when(statement).setLong(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.query(setter, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(statement).close();
        verify(connection).close();
    }
}
