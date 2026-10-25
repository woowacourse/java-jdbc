package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;
    private final RowMapper<String> accountRowMapper = (resultSet) ->
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
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);

        // when: 실제 테스트 대상 메서드를 실행한다.
        jdbcTemplate.update(sql, "password", 3L);

        // then: 각 인자의 바인딩 위치와 SQL 실행 여부를 확인한다.
        verify(statement).setObject(1, "password");
        verify(statement).setObject(2, 3L);
        verify(statement).executeUpdate();
    }

    @Test
    void update_CustomSetter_UsesCustomBindingAndExecutesUpdate() throws SQLException {
        final var sql = "update users set password = ? where id = ?";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setString(1, "password");
            pstmt.setLong(2, 3L);
        };

        jdbcTemplate.update(sql, setter);

        verify(statement).setString(1, "password");
        verify(statement).setLong(2, 3L);
        verify(statement).executeUpdate();
    }

    @Test
    void update_ExternalConnection_ClosesStatementAndKeepsConnectionOpen() throws SQLException {
        final var sql = "update users set password = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);

        jdbcTemplate.update(sql, connection, "password", 3L);

        verify(statement).setObject(1, "password");
        verify(statement).setObject(2, 3L);
        verify(statement).executeUpdate();
        verify(statement).close();
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verifyNoInteractions(dataSource);
    }

    @Test
    void update_ExternalConnectionExecutionFails_PreservesCauseAndKeepsConnectionOpen() throws SQLException {
        final var sql = "update users set password = ? where id = ?";
        final var failure = new SQLException("SQL execution failed");
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(failure);
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setString(1, "password");
            pstmt.setLong(2, 3L);
        };

        assertThatThrownBy(() -> jdbcTemplate.update(sql, connection, setter))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement).setString(1, "password");
        verify(statement).setLong(2, 3L);
        verify(statement).close();
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verifyNoInteractions(dataSource);
    }

    @Test
    void query_OneRow_ReturnsSingleMappedObjectInList() throws SQLException {
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
        assertThat(result).containsExactly("gugu");
    }

    @Test
    void query_NoRows_DoesNotInvokeRowMapper() throws SQLException {
        final var sql = "select id, account, password, email from users where id = ?";
        final var param = 1L;
        final RowMapper<?> rowMapper = mock(RowMapper.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        jdbcTemplate.query(sql, rowMapper, param);

        verify(statement).setObject(1, 1L);
        verify(statement).executeQuery();
        verifyNoInteractions(rowMapper);
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
    void query_CustomSetter_UsesCustomBindingAndReturnsMappedObjects() throws SQLException {
        final var sql = "select account from users where id = ?";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");
        PreparedStatementSetter setter = pstmt -> pstmt.setLong(1, 1L);

        List<String> result = jdbcTemplate.query(sql, setter, accountRowMapper);

        verify(statement).setLong(1, 1L);
        verify(statement).executeQuery();
        assertThat(result).containsExactly("gugu");
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
    void update_ExecutionFails_ThrowsDataAccessExceptionAndClosesResources() throws SQLException {
        final var sql = "update users set password = ? where id = ?";
        final var exception = new SQLException("SQL execution failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "password", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void query_RowMappingFails_ThrowsDataAccessExceptionAndClosesResources() throws SQLException {
        final var sql = "select account from users";
        final var exception = new SQLException("Row mapping failed");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenThrow(exception);

        assertThatThrownBy(() -> jdbcTemplate.query(sql, accountRowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(exception);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
