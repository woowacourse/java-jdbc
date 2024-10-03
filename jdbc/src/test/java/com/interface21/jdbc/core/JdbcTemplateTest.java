package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.InOrder;
import samples.TestUser;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Nested
    @DisplayName("executeUpdate")
    class ExecuteUpdateTests {

        @DisplayName("업데이트 쿼리를 실행한다.")
        @Test
        void executeUpdate() throws SQLException {
            // given
            String sql = "update users set account = ? where id = ?";

            // when
            jdbcTemplate.executeUpdate(sql, "mia", 1L);

            // then
            InOrder inOrder = inOrder(connection, preparedStatement);
            inOrder.verify(connection).prepareStatement(sql);
            inOrder.verify(preparedStatement).setObject(1, "mia");
            inOrder.verify(preparedStatement).setObject(2, 1L);
            inOrder.verify(preparedStatement).executeUpdate();
            inOrder.verify(preparedStatement).close();
            inOrder.verify(connection).close();
        }

        @DisplayName("업데이트 쿼리 실행 실패 시 예외를 발생한다.")
        @Test
        void executeUpdate_throwsException() throws SQLException {
            // given
            String sql = "update users set account = ? where id = ?";
            when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);

            // when & then
            assertThatThrownBy(() -> jdbcTemplate.executeUpdate(sql, "mia", 1L))
                    .isInstanceOf(RuntimeException.class);
            verify(preparedStatement).close();
            verify(connection).close();
        }
    }

    @Nested
    @DisplayName("fetchResults")
    class FetchResultsTests {

        @DisplayName("여러 결과를 조회한다.")
        @Test
        void fetchResults() throws SQLException {
            // given
            String sql = "select * from users";
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getLong("id")).thenReturn(1L, 2L);
            when(resultSet.getString("account")).thenReturn("mia", "nyangin");

            // when
            List<TestUser> actual = jdbcTemplate.fetchResults(sql, JdbcTemplateTest.this::userResultMapper);

            // then
            assertAll(
                    () -> assertThat(actual).hasSize(2),
                    () -> assertThat(actual.get(0).getId()).isEqualTo(1L),
                    () -> assertThat(actual.get(0).getAccount()).isEqualTo("mia"),
                    () -> assertThat(actual.get(1).getId()).isEqualTo(2L),
                    () -> assertThat(actual.get(1).getAccount()).isEqualTo("nyangin")
            );
            verify(resultSet).close();
            verify(preparedStatement).close();
            verify(connection).close();
        }

        @DisplayName("여러 결과 조회 실패 시 예외를 발생한다.")
        @Test
        void fetchResults_throwsException() throws SQLException {
            // given
            String sql = "select * from users";
            when(preparedStatement.executeQuery()).thenThrow(SQLException.class);

            // when & then
            assertThatThrownBy(() -> jdbcTemplate.fetchResults(sql, JdbcTemplateTest.this::userResultMapper))
                    .isInstanceOf(RuntimeException.class);
            verify(preparedStatement).close();
            verify(connection).close();
        }
    }

    @Nested
    @DisplayName("fetchResult")
    class FetchResultTests {

        @DisplayName("단일 결과를 조회한다.")
        @Test
        void fetchResult() throws SQLException {
            // given
            String sql = "select * from users where id = ?";
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getLong("id")).thenReturn(1L);
            when(resultSet.getString("account")).thenReturn("mia");

            // when
            TestUser actual = jdbcTemplate.fetchResult(sql, JdbcTemplateTest.this::userResultMapper, 1L);

            // then
            assertAll(
                    () -> assertThat(actual.getId()).isEqualTo(1L),
                    () -> assertThat(actual.getAccount()).isEqualTo("mia")
            );
            verify(resultSet).close();
            verify(preparedStatement).close();
            verify(connection).close();
        }

        @DisplayName("단일 결과 조회 실패 시 예외를 발생한다.")
        @Test
        void fetchResult_throwsException() throws SQLException {
            // given
            String sql = "select * from users where id = ?";
            when(preparedStatement.executeQuery()).thenThrow(SQLException.class);

            // when & then
            assertThatThrownBy(() -> jdbcTemplate.fetchResult(sql, JdbcTemplateTest.this::userResultMapper, 1L))
                    .isInstanceOf(RuntimeException.class);
            verify(preparedStatement).close();
            verify(connection).close();
        }
    }

    TestUser userResultMapper(ResultSet resultSet) {
        try {
            return new TestUser(
                    resultSet.getLong("id"),
                    resultSet.getString("account")
            );
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }
}

