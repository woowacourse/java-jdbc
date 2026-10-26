package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    @Test
    @DisplayName("가변 인자를 순서대로 바인딩하고 수정된 행 수를 반환한다")
    void updateBindsArgumentsInOrder() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        int affectedRows = jdbcTemplate.update(
                "update users set password = ? where id = ?", "password99", 1L);

        assertThat(affectedRows).isEqualTo(1);
        var order = inOrder(statement);
        order.verify(statement).setObject(1, "password99");
        order.verify(statement).setObject(2, 1L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("setter로 SQL 타입을 지정해 바인딩하고 수정된 행 수를 반환한다")
    void updateUsesCustomSetter() throws SQLException {
        Date date = Date.valueOf("2026-01-01");
        when(statement.executeUpdate()).thenReturn(2);

        int affectedRows = jdbcTemplate.update(
                "update events set description = ?, event_date = ?",
                pstmt -> {
                    pstmt.setNull(1, Types.VARCHAR);
                    pstmt.setDate(2, date);
                });

        assertThat(affectedRows).isEqualTo(2);
        var order = inOrder(statement);
        order.verify(statement).setNull(1, Types.VARCHAR);
        order.verify(statement).setDate(2, date);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("Statement 준비가 실패하면 원인을 보존하고 Connection을 닫는다")
    void statementPreparationFailureClosesConnection() throws SQLException {
        SQLException cause = new SQLException("Statement 준비 실패");
        when(connection.prepareStatement(anyString())).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users where id = ?", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
        verifyNoInteractions(statement);
    }

    @Test
    @DisplayName("가변 인자 바인딩이 실패하면 실행하지 않고 원인을 보존하며 자원을 닫는다")
    void argumentBindingFailureClosesResources() throws SQLException {
        SQLException cause = new SQLException("파라미터 바인딩 실패");
        doThrow(cause).when(statement).setObject(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users where id = ?", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("setter 바인딩이 실패하면 실행하지 않고 원인을 보존하며 자원을 닫는다")
    void setterBindingFailureClosesResources() throws SQLException {
        SQLException cause = new SQLException("setter 바인딩 실패");
        doThrow(cause).when(statement).setLong(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "delete from users where id = ?", pstmt -> pstmt.setLong(1, 1L)))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("수정 SQL 실행이 실패하면 원인을 보존하고 자원을 닫는다")
    void updateFailureClosesResources() throws SQLException {
        SQLException cause = new SQLException("SQL 실행 실패");
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users where id = ?", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("목록 조회의 결과 변환이 실패하면 원인을 보존하고 자원을 닫는다")
    void queryMappingFailureClosesResources() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        SQLException cause = new SQLException("조회 결과 변환 실패");
        RowMapper<String> rowMapper = rs -> {
            throw cause;
        };

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회의 결과 변환이 실패하면 원인을 보존하고 자원을 닫는다")
    void queryForObjectMappingFailureClosesResources() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        SQLException cause = new SQLException("조회 결과 변환 실패");
        RowMapper<String> rowMapper = rs -> {
            throw cause;
        };

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?", rowMapper, 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("단건 조회 결과가 없으면 null을 반환한다")
    void queryForObjectReturnsNullWhenNoRows() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        String result = jdbcTemplate.queryForObject(
                "select account from users where account = ?",
                rs -> rs.getString("account"), "gugu");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("단건 조회 결과가 1건이면 변환한 객체를 반환한다")
    void queryForObjectReturnsMappedObjectWhenOneRow() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        String result = jdbcTemplate.queryForObject(
                "select account from users where account = ?",
                rs -> rs.getString("account"), "gugu");

        assertThat(result).isEqualTo("gugu");
    }

    @Test
    @DisplayName("단건 조회 결과가 2건 이상이면 예외를 던지고 자원을 닫는다")
    void queryForObjectRejectsMultipleRows() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true);
        when(resultSet.getString("account")).thenReturn("gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where account = ?",
                rs -> rs.getString("account"), "gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("단건 조회 결과는 최대 1건이어야 합니다.");

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
