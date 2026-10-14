package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("수정 쿼리를 실행한다")
        void executeUpdateQuery() throws SQLException {
            String sql = "UPDATE users SET name = '피카츄'";

            jdbcTemplate.update(sql, List.of());

            verify(connection).prepareStatement(sql);
            verify(statement).executeUpdate();
        }

        @Test
        @DisplayName("파라미터를 List 순서대로 인덱스에 바인딩한다")
        void bindParametersToIndexesInListOrder() throws SQLException {
            String sql = "UPDATE users SET name = ? WHERE id = ?";

            jdbcTemplate.update(sql, List.of("피카츄", 1L));

            verify(statement).setObject(1, "피카츄");
            verify(statement).setObject(2, 1L);
        }
    }

    @Nested
    @DisplayName("select")
    class Select {

        @Test
        @DisplayName("조회 쿼리를 실행하고 변환 결과를 반환한다")
        void executeSelectQuery() throws SQLException {
            String sql = "SELECT name FROM users";
            when(statement.executeQuery()).thenReturn(resultSet);

            String actual = jdbcTemplate.select(sql, currentResultSet -> {
                assertThat(currentResultSet).isSameAs(resultSet);
                return "피카츄";
            });

            assertThat(actual).isEqualTo("피카츄");
            verify(connection).prepareStatement(sql);
            verify(statement).executeQuery();
        }

        @Test
        @DisplayName("파라미터를 List 순서대로 인덱스에 바인딩한다")
        void bindParametersToIndexesInListOrder() throws SQLException {
            String sql = "SELECT name FROM users WHERE age = ? AND id = ?";
            when(statement.executeQuery()).thenReturn(resultSet);

            jdbcTemplate.select(sql, List.of(10, 1L), currentResultSet -> "피카츄");

            verify(statement).setObject(1, 10);
            verify(statement).setObject(2, 1L);
        }

        @Test
        @DisplayName("결과 변환 중 예외가 발생해도 연결을 닫는다")
        void closeConnectionWhenResultTransformationFails() throws SQLException {
            String sql = "SELECT name FROM users";
            SQLException exception = new SQLException("결과 변환 실패");
            when(statement.executeQuery()).thenReturn(resultSet);
            when(resultSet.getString("name")).thenThrow(exception);

            assertThatThrownBy(() -> jdbcTemplate.select(sql, currentResultSet -> currentResultSet.getString("name")))
                    .isInstanceOf(RuntimeException.class)
                    .hasCause(exception);
            verify(statement).close();
            verify(connection).close();
        }
    }
}
