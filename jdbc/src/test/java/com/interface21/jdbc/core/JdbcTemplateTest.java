package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "update records set note = ? where id = ?";

    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void bindsNullParameter() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update(SQL, null, 1L)).isEqualTo(1);

        verify(statement).setObject(1, null);
        verify(statement).setObject(2, 1L);
    }

    @Test
    void wrapsSqlExceptionAndClosesResources() throws SQLException {
        SQLException failure = new SQLException("update failed");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, "note", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure)
                .hasMessageContaining(SQL);

        verify(statement).close();
        verify(connection).close();
    }
    @Test
    void rejectsMultipleRowsForSingleResult() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true);

        assertThatThrownBy(() -> jdbcTemplate.queryFirst(SQL, row -> "user"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("2개 이상");

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
    @TestFactory
    Stream<DynamicTest> closesQueryResources() {
        return Stream.of(false, true).flatMap(singleResult ->
                Stream.of("empty", "success", "sqlFailure", "mappingFailure").map(scenario ->
                        DynamicTest.dynamicTest(
                                (singleResult ? "queryFirst" : "query") + " - " + scenario,
                                () -> {
                                    setUp();
                                    ResultSet resultSet = mock(ResultSet.class);
                                    when(statement.executeQuery()).thenReturn(resultSet);
                                    when(resultSet.next()).thenReturn(!scenario.equals("empty"), false);
                                    RowMapper<String> mapper = row -> "user";
                                    if (scenario.equals("sqlFailure")) {
                                        when(resultSet.next()).thenThrow(new SQLException("read failed"));
                                    }
                                    if (scenario.equals("mappingFailure")) {
                                        mapper = row -> {
                                            throw new IllegalStateException("mapping failed");
                                        };
                                    }

                                    RowMapper<String> rowMapper = mapper;
                                    ThrowingCallable query = () -> {
                                        if (singleResult) {
                                            jdbcTemplate.queryFirst(SQL, rowMapper);
                                        } else {
                                            jdbcTemplate.query(SQL, rowMapper);
                                        }
                                    };
                                    if (scenario.equals("sqlFailure")) {
                                        assertThatThrownBy(query).isInstanceOf(DataAccessException.class);
                                    } else if (scenario.equals("mappingFailure")) {
                                        assertThatThrownBy(query).isInstanceOf(IllegalStateException.class);
                                    } else {
                                        query.call();
                                    }

                                    verify(resultSet).close();
                                    verify(statement).close();
                                    verify(connection).close();
                                })));
    }
}
