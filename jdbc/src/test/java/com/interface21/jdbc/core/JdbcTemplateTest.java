package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;
    private RowMapper<String> accountRowMapper = (resultSet) ->
            resultSet.getString("account");

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update_MultipleParameters_BindsParametersAndExecutesUpdate() throws SQLException {
        // given: JDBC 객체 사이의 연결을 설정한다.
        final var sql = "update users set password = ? where id = ?";
        Object[] params = {"password", 3L};
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);

        // when: 실제 테스트 대상 메서드를 실행한다.
        jdbcTemplate.update(sql, params);

        // then: 각 인자의 바인딩 위치와 SQL 실행 여부를 확인한다.
        verify(statement).setObject(1, "password");
        verify(statement).setObject(2, 3L);
        verify(statement).executeUpdate();
    }

    @Test
    void queryForObject_OneRow_ReturnsMappedObject() throws SQLException {
        final var sql = "select id, account, password, email from users where id = ?";
        final var param = 1L;
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenReturn("gugu");

        String result = jdbcTemplate.queryForObject(sql, accountRowMapper, param);

        verify(statement).setObject(1, 1L);
        verify(statement).executeQuery();
        assertThat(result).isEqualTo("gugu");
    }

    @Test
    void queryForObject_NoRows_ReturnsNull() throws SQLException {
        final var sql = "select id, account, password, email from users where id = ?";
        final var param = 1L;
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        String result = jdbcTemplate.queryForObject(sql, accountRowMapper, param);

        verify(statement).setObject(1, 1L);
        verify(statement).executeQuery();
        assertThat(result).isNull();
    }

    @Test
    void query_MultipleRows_ReturnsAllMappedObjects() throws SQLException {
        final var sql = "select id, account, password, email from users";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "Ethan");

        List<String> result = jdbcTemplate.query(sql, accountRowMapper);

        verify(statement).executeQuery();
        assertThat(result).containsExactly("gugu", "Ethan");
    }

    @Test
    void query_NoRows_ReturnsEmptyList() throws SQLException {
        final var sql = "select id, account, password, email from users";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<String> result = jdbcTemplate.query(sql, accountRowMapper);

        verify(statement).executeQuery();
        assertThat(result).isEmpty();
    }

    @Test
    void query_WithParameters_BindsParameters() throws SQLException {
        final var sql = "select id, account, password, email from users where id = ?";
        final var param = 1L;
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        List<String> result = jdbcTemplate.query(sql, accountRowMapper, param);

        verify(statement).setObject(1, 1L);
        verify(statement).executeQuery();
        assertThat(result.getFirst()).isEqualTo("gugu");
    }

    @Test
    void query_Success_ClosesResources() throws SQLException {
        final var sql = "select account from users";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        jdbcTemplate.query(sql, accountRowMapper);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void update_ExecutionFails_ThrowsRuntimeExceptionAndClosesResources() throws SQLException {
        final var sql = "update users set password = ? where id = ?";
        final var exception = new SQLException("SQL execution failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "password", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasCause(exception);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void query_RowMappingFails_ThrowsRuntimeExceptionAndClosesResources() throws SQLException {
        final var sql = "select account from users";
        final var exception = new SQLException("Row mapping failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.query(sql, accountRowMapper))
                .isInstanceOf(RuntimeException.class)
                .hasCause(exception);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
