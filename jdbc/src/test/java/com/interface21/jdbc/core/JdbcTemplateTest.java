package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SELECT_SQL = "select name from items";
    private static final String UPDATE_SQL = "update items set name = ? where id = ?";

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
        when(dataSource.getConnection()).thenReturn(connection);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("조회 결과가 한 개이면 매핑한 값을 반환한다")
    void queryForObjectReturnsMappedValue() throws SQLException {
        prepareQuery();
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("apple");

        final String actual = jdbcTemplate.queryForObject(SELECT_SQL, rs -> rs.getString("name"));

        assertThat(actual).isEqualTo("apple");
    }

    @Test
    @DisplayName("조회 결과가 없으면 null을 반환한다")
    void queryForObjectReturnsNullWhenEmpty() throws SQLException {
        prepareQuery();
        when(resultSet.next()).thenReturn(false);

        final String actual = jdbcTemplate.queryForObject(SELECT_SQL, rs -> rs.getString("name"));

        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("조회 결과가 두 개 이상이면 예외를 던진다")
    void queryForObjectRejectsMultipleRows() throws SQLException {
        prepareQuery();
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SELECT_SQL, rs -> "apple"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("조회 결과는 최대 1개여야 합니다. 실제 결과 수: 2");
    }

    @Test
    @DisplayName("모든 행을 순서대로 매핑하고 조회 자원을 닫는다")
    void queryMapsAllRowsAndClosesResources() throws SQLException {
        prepareQuery();
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("apple", "banana");

        final var actual = jdbcTemplate.query(SELECT_SQL, rs -> rs.getString("name"));

        assertThat(actual).containsExactly("apple", "banana");
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("수정 인자를 위치에 맞게 설정하고 영향받은 행 수를 반환한다")
    void updateBindsParametersAndReturnsAffectedRows() throws SQLException {
        when(connection.prepareStatement(UPDATE_SQL)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(2);

        final int actual = jdbcTemplate.update(UPDATE_SQL, "apple", 7L);

        assertThat(actual).isEqualTo(2);
        verify(statement).setObject(1, "apple");
        verify(statement).setObject(2, 7L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("조회에서도 전달한 인자를 설정한다")
    void queryBindsParameters() throws SQLException {
        final String sql = SELECT_SQL + " where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThat(jdbcTemplate.query(sql, rs -> rs.getString("name"), 7L)).isEmpty();

        verify(statement).setObject(1, 7L);
    }

    @Test
    @DisplayName("SQL 실행 예외를 원인을 보존하여 변환하고 자원을 닫는다")
    void updateTranslatesSqlExceptionAndClosesResources() throws SQLException {
        final var cause = new SQLException("실행 실패");
        when(connection.prepareStatement(UPDATE_SQL)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "apple", 7L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("행 매핑 중 SQL 예외가 발생해도 모든 조회 자원을 닫는다")
    void queryClosesResourcesWhenMappingFails() throws SQLException {
        prepareQuery();
        final var cause = new SQLException("매핑 실패");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("name")).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, rs -> rs.getString("name")))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("Statement 생성이 실패해도 열린 연결을 닫는다")
    void closesConnectionWhenStatementCreationFails() throws SQLException {
        final var cause = new SQLException("Statement 생성 실패");
        when(connection.prepareStatement(UPDATE_SQL)).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, "apple", 7L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);

        verify(connection).close();
    }

    private void prepareQuery() throws SQLException {
        when(connection.prepareStatement(SELECT_SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }
}
