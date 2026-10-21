package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
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
    @DisplayName("selectMany")
    class SelectMany {

        @BeforeEach
        void setUpResultSet() throws SQLException {
            when(statement.executeQuery()).thenReturn(resultSet);
        }

        @Test
        @DisplayName("각 행을 변환한 불변 목록을 반환한다")
        void mapRowsToImmutableList() throws SQLException {
            String sql = "SELECT name FROM users";
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getString("name")).thenReturn("피카츄", "라이츄");

            List<String> actual = jdbcTemplate.selectMany(sql, currentResultSet -> currentResultSet.getString("name"));

            assertThat(actual).containsExactly("피카츄", "라이츄");
            assertThatThrownBy(() -> actual.add("파이리")).isInstanceOf(UnsupportedOperationException.class);
            verify(connection).prepareStatement(sql);
            verify(statement).executeQuery();
            verify(resultSet).close();
            verify(statement).close();
            verify(connection).close();
        }

        @Test
        @DisplayName("조회 결과가 없으면 수정 불가 빈 목록을 반환한다")
        void returnUnmodifiableEmptyList() {
            List<String> actual = jdbcTemplate.selectMany("SELECT name FROM users", currentResultSet -> "피카츄");

            assertThat(actual).isEmpty();
            assertThatThrownBy(() -> actual.add("피카츄")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("매퍼가 null을 반환하면 예외를 던진다")
        void rejectNullMappedRow() throws SQLException {
            when(resultSet.next()).thenReturn(true, false);

            assertThatThrownBy(() -> jdbcTemplate.selectMany("SELECT name FROM users", currentResultSet -> null))
                    .isInstanceOf(NullPointerException.class);
            verify(resultSet).close();
            verify(statement).close();
            verify(connection).close();
        }

        @Test
        @DisplayName("파라미터를 List 순서대로 인덱스에 바인딩한다")
        void bindParametersToIndexesInListOrder() throws SQLException {
            String sql = "SELECT name FROM users WHERE age = ? AND id = ?";

            jdbcTemplate.selectMany(sql, List.of(10, 1L), currentResultSet -> "피카츄");

            verify(statement).setObject(1, 10);
            verify(statement).setObject(2, 1L);
        }

        @Test
        @DisplayName("결과 변환 중 예외가 발생해도 연결을 닫는다")
        void closeConnectionWhenResultTransformationFails() throws SQLException {
            String sql = "SELECT name FROM users";
            SQLException exception = new SQLException("결과 변환 실패");
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getString("name")).thenThrow(exception);

            assertThatThrownBy(() -> jdbcTemplate.selectMany(sql, currentResultSet -> currentResultSet.getString("name")))
                    .isInstanceOf(DataAccessException.class)
                    .hasCause(exception);
            verify(resultSet).close();
            verify(statement).close();
            verify(connection).close();
        }
    }

    @Nested
    @DisplayName("selectOne")
    class SelectOne {

        @BeforeEach
        void setUpResultSet() throws SQLException {
            when(statement.executeQuery()).thenReturn(resultSet);
        }

        @Test
        @DisplayName("조회 결과가 없으면 Optional.empty를 반환한다")
        void returnEmptyOptional() {
            Optional<String> actual = jdbcTemplate.selectOne("SELECT name FROM users", currentResultSet -> "피카츄");

            assertThat(actual).isEmpty();
        }

        @Test
        @DisplayName("한 행을 변환해서 Optional로 반환하고 파라미터를 순서대로 바인딩한다")
        void returnMappedRowAndBindParameters() throws SQLException {
            String sql = "SELECT name FROM users WHERE age = ? AND id = ?";
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString("name")).thenReturn("피카츄");

            Optional<String> actual = jdbcTemplate.selectOne(sql, List.of(10, 1L),
                    currentResultSet -> currentResultSet.getString("name"));

            assertThat(actual).contains("피카츄");
            verify(statement).setObject(1, 10);
            verify(statement).setObject(2, 1L);
        }

        @Test
        @DisplayName("두 행 이상이면 예외를 던지고 자원을 닫는다")
        void rejectMultipleRows() throws SQLException {
            when(resultSet.next()).thenReturn(true, true, false);

            assertThatThrownBy(() -> jdbcTemplate.selectOne("SELECT name FROM users", currentResultSet -> "피카츄"))
                    .isInstanceOf(DataAccessException.class);
            verify(resultSet).close();
            verify(statement).close();
            verify(connection).close();
        }
    }
}
