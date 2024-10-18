package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataAccessWrapper;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

class JdbcTemplateTest {

    private ArgumentCaptor<String> argumentCaptor;
    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private PreparedStatementResolver resolver;
    private RowMapper<Object> rowMapper;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        argumentCaptor = ArgumentCaptor.forClass(String.class);
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        resolver = mock(PreparedStatementResolver.class);
        rowMapper = mock(RowMapper.class);

        TransactionSynchronizationManager.bindResource(dataSource, connection);
        doReturn(connection).when(dataSource).getConnection();
        when(connection.prepareStatement(argumentCaptor.capture())).thenReturn(preparedStatement);
        when(resolver.resolve(any(), any())).thenReturn(preparedStatement);
        when(preparedStatement.getResultSet()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(new DataAccessWrapper(dataSource), resolver);
    }

    @DisplayName("query를 통해 얻은 결과를 담아 List 형태로 반환한다")
    @Test
    void query() throws SQLException {
        String sql = "any valid Sql Query";
        setUpQueryResults(List.of("test1", "test2", "test3"));

        List<String> results = jdbcTemplate.query(sql, rowMapper).stream()
                .map(String::valueOf)
                .toList();

        assertAll(
                () -> assertThat(argumentCaptor.getValue()).isEqualTo(sql),
                () -> assertThat(results).containsExactly("test1", "test2", "test3"),
                () -> verify(preparedStatement, times(1)).executeQuery()
        );
    }

    @DisplayName("query에서 Exception이 발생할 경우, DataAccessException으로 전환된다")
    @Test
    void throwDataAccessException_When_queryThrowException() throws SQLException {
        String sql = "any invalid Sql Query";
        setUpException();

        assertThatThrownBy(() -> jdbcTemplate.query(sql, mock(RowMapper.class)))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("query를 통해 얻은 결과 하나를 반환한다")
    @Test
    void queryForObject() throws SQLException {
        String sql = "any valid Sql Query";
        setUpQueryResults(List.of("test1"));

        Object result = jdbcTemplate.queryForObject(sql, rowMapper);

        assertAll(
                () -> assertThat(argumentCaptor.getValue()).isEqualTo(sql),
                () -> assertThat(result).isEqualTo("test1"),
                () -> verify(preparedStatement, times(1)).executeQuery()
        );
    }

    @DisplayName("query를 통해 얻은 결과가 없을 경우 DataAccessException을 반환한다")
    @Test
    void throwDataAccessException_When_queryForObjectReturnEmptyResult() throws SQLException {
        String sql = "any valid Sql Query";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("결과가 없습니다.");
    }

    @DisplayName("query를 통해 얻은 결과가 하나 이상일 경우, DataAccessException을 반환한다")
    @Test
    void throwDataAccessException_When_queryForObjectReturnMoreThanOneResult() throws SQLException {
        String sql = "any valid Sql Query";
        setUpQueryResults(List.of("test1", "test2"));

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("결과가 2개 이상입니다.");
    }

    @DisplayName("executeUpdate를 통해 DB를 업데이트 한다")
    @Test
    void queryForUpdate() {
        String sql = "any valid Sql Query";

        jdbcTemplate.queryForUpdate( sql, "test");

        assertAll(
                () -> assertThat(argumentCaptor.getValue()).isEqualTo(sql),
                () -> verify(preparedStatement, times(1)).executeUpdate()
        );
    }

    @DisplayName("queryForUpdate에서 Exception이 발생할 경우, DataAccessException으로 전환된다")
    @Test
    void throwDataAccessException_When_queryForUpdateThrowException() throws SQLException {
        String sql = "any invalid Sql Query";
        setUpException();

        assertThatThrownBy(() -> jdbcTemplate.queryForUpdate(sql, "test"))
                .isInstanceOf(DataAccessException.class);
    }

    private void setUpException() throws SQLException {
        Mockito.reset(connection);
        when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
    }

    private void setUpQueryResults(List<Object> results) throws SQLException {
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        OngoingStubbing<Boolean> hasResultStub = when(resultSet.next());

        for (int i = 0; i < results.size(); i++) {
            hasResultStub = hasResultStub.thenReturn(true);
        }
        hasResultStub.thenReturn(false);

        when(rowMapper.mapRow(any(ResultSet.class)))
                .thenReturn(results.get(0), results.subList(1, results.size()).toArray());
    }
}
