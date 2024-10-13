package com.interface21.jdbc.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.interface21.dao.NoResultFoundException;
import com.interface21.dao.NotSingleResultException;

class JdbcTemplateTest {

    DataSource dataSource = mock(DataSource.class);
    Connection conn = mock(Connection.class);
    PreparedStatement preparedStatement = mock(PreparedStatement.class);
    JdbcTemplate jdbcTemplate = new JdbcTemplate();
    TransactionManager transactionManager = new TransactionManager(dataSource);

    @BeforeEach
    void init() throws SQLException {
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Nested
    @DisplayName("getResult 테스트")
    class GetResultTest {

        @Test
        @DisplayName("여러건의 데이터가 조회되면 예외가 발생한다.")
        void notSingleResultTest() throws SQLException {
            ResultSet resultSet = mock(ResultSet.class);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true).thenReturn(true);

            assertThatThrownBy(() -> transactionManager.getResultInTransaction(conn ->
                    jdbcTemplate.getResult(conn, "query", (rs, rowNum) -> new Object()))
            ).isInstanceOf(NotSingleResultException.class);
        }

        @Test
        @DisplayName("데이터가 조회되지 않으면 예외가 발생한다.")
        void noResultTest() throws SQLException {
            ResultSet resultSet = mock(ResultSet.class);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.getRow()).thenReturn(0);
            when(resultSet.next()).thenReturn(false);

            assertThatThrownBy(() -> transactionManager.getResultInTransaction(
                    conn -> jdbcTemplate.getResult(conn, "query", (rs, rowNum) -> new Object())))
                    .isInstanceOf(NoResultFoundException.class);
        }
    }
}
