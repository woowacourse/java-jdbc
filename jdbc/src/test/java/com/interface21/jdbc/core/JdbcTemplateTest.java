package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {

    private static final RowMapper<String> NAME_ROW_MAPPER = rs -> rs.getString("name");

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

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(statement);
        given(statement.executeQuery()).willReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    @DisplayName("query는 파라미터를 바인딩하고 모든 행을 RowMapper로 변환한다")
    void query() throws SQLException {
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString("name")).willReturn("gugu", "woody");

        final List<String> result = jdbcTemplate.query("select name from users where id > ?", NAME_ROW_MAPPER, 0L);

        assertThat(result).containsExactly("gugu", "woody");
        verify(statement).setObject(1, 0L);
    }

    @Test
    @DisplayName("query는 사용한 자원을 모두 닫는다")
    void queryClosesResources() throws SQLException {
        given(resultSet.next()).willReturn(false);

        jdbcTemplate.query("select name from users", NAME_ROW_MAPPER);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("queryForObject는 결과가 1건이면 해당 객체를 반환한다")
    void queryForObject() throws SQLException {
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString("name")).willReturn("gugu");

        final String result = jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L);

        assertThat(result).isEqualTo("gugu");
        verify(statement).setObject(1, 1L);
    }

    @Test
    @DisplayName("queryForObject는 결과가 없으면 예외를 던진다")
    void queryForObjectWithNoResult() throws SQLException {
        given(resultSet.next()).willReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L))
                .isInstanceOf(EmptyResultDataAccessException.class)
                .satisfies(e -> {
                    final var exception = (EmptyResultDataAccessException) e;
                    assertThat(exception.getExpectedSize()).isEqualTo(1);
                    assertThat(exception.getActualSize()).isEqualTo(0);
                });
    }

    @Test
    @DisplayName("queryForObject는 결과가 2건 이상이면 예외를 던진다")
    void queryForObjectWithMultipleResults() throws SQLException {
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString("name")).willReturn("gugu", "gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from users where name = ?", NAME_ROW_MAPPER, "gugu"))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class)
                .satisfies(e -> {
                    final var exception = (IncorrectResultSizeDataAccessException) e;
                    assertThat(exception.getExpectedSize()).isEqualTo(1);
                    assertThat(exception.getActualSize()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("update는 파라미터를 순서대로 바인딩하고 영향받은 행 수를 반환한다")
    void update() throws SQLException {
        given(statement.executeUpdate()).willReturn(1);

        final int affectedRows = jdbcTemplate.update("update users set password = ? where id = ?", "password99", 1L);

        assertThat(affectedRows).isEqualTo(1);
        verify(statement).setObject(1, "password99");
        verify(statement).setObject(2, 1L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("query는 전달받은 PreparedStatementSetter로 파라미터를 바인딩한다")
    void queryWithPreparedStatementSetter() throws SQLException {
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getString("name")).willReturn("gugu");

        final List<String> result = jdbcTemplate.query(
                "select name from users where id = ?",
                ps -> ps.setLong(1, 1L),
                NAME_ROW_MAPPER);

        assertThat(result).containsExactly("gugu");
        verify(statement).setLong(1, 1L);
    }

    @Test
    @DisplayName("update는 전달받은 PreparedStatementSetter로 파라미터를 바인딩한다")
    void updateWithPreparedStatementSetter() throws SQLException {
        given(statement.executeUpdate()).willReturn(1);

        final int affectedRows = jdbcTemplate.update("update users set email = ? where id = ?", ps -> {
            ps.setNull(1, Types.VARCHAR);
            ps.setLong(2, 1L);
        });

        assertThat(affectedRows).isEqualTo(1);
        verify(statement).setNull(1, Types.VARCHAR);
        verify(statement).setLong(2, 1L);
    }

    @Test
    @DisplayName("PreparedStatementSetter에서 SQLException이 발생하면 DataAccessException으로 감싸고 자원을 닫는다")
    void wrapSQLExceptionFromPreparedStatementSetter() throws SQLException {
        final SQLException cause = new SQLException("invalid parameter");

        assertThatThrownBy(() -> jdbcTemplate.update("update users set email = ?", ps -> {
            throw cause;
        }))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("RowMapper에서 SQLException이 발생하면 DataAccessException으로 감싸고 자원을 닫는다")
    void wrapSQLExceptionFromRowMapper() throws SQLException {
        final SQLException cause = new SQLException("mapping failed");
        given(resultSet.next()).willReturn(true);
        given(resultSet.getString("name")).willThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from users", NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("SQLException이 발생하면 DataAccessException으로 감싸서 던진다")
    void wrapSQLException() throws SQLException {
        final SQLException cause = new SQLException("connection failed");
        given(dataSource.getConnection()).willThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query("select name from users", NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
    }
}
