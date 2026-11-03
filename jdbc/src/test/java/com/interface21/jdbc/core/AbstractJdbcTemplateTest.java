package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractJdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final String sql = "update users set account = ?";
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
            statement.setString(1, "gugu");
        }
    };

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
    }

    @Test
    @DisplayName("변경 건수가 0이면 그대로 반환하고 자원을 해제한다")
    void update_with_no_affected_rows() throws SQLException {
        assertThat(jdbcTemplate.update()).isZero();

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("쿼리를 준비하다 실패하면 SQL과 원인을 보존하고 커넥션을 해제한다")
    void prepare_statement_failure() throws SQLException {
        final var failure = new SQLException("쿼리 준비 실패");
        when(connection.prepareStatement(sql)).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update())
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining(sql)
                .hasCause(failure);
        verify(connection).close();
    }

    @Test
    @DisplayName("값을 설정하다 런타임 예외가 발생하면 같은 예외를 던지고 자원을 해제한다")
    void set_values_runtime_failure() throws SQLException {
        final var failure = new IllegalArgumentException("계정 확인 실패");
        doThrow(failure).when(statement).setString(1, "gugu");

        assertThatThrownBy(() -> jdbcTemplate.update()).isSameAs(failure);
        verify(statement).close();
        verify(connection).close();
    }
}
