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
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<String> NAME_ROW_MAPPER = rs -> rs.getString("name");

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("update는 인자를 순서대로 바인딩하고 영향받은 행 수를 반환한다")
    @Test
    void update() throws SQLException {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final int updatedCount = jdbcTemplate.update("update users set email = ? where id = ?", "a@b.com", 1L);

        assertThat(updatedCount).isEqualTo(1);
        verify(preparedStatement).setObject(1, "a@b.com");
        verify(preparedStatement).setObject(2, 1L);
    }

    @DisplayName("update는 PreparedStatementSetter로 값을 직접 바인딩할 수 있다")
    @Test
    void updateWithPreparedStatementSetter() throws SQLException {
        jdbcTemplate.update("delete from users where id = ?", pstmt -> pstmt.setLong(1, 1L));

        verify(preparedStatement).setLong(1, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("update가 끝나면 PreparedStatement와 Connection을 닫는다")
    @Test
    void updateClosesResources() throws SQLException {
        jdbcTemplate.update("delete from users");

        verify(preparedStatement).close();
        verify(connection).close();
    }

    @DisplayName("query는 조회된 모든 행을 RowMapper로 변환해 반환한다")
    @Test
    void query() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("gugu", "dahye");

        final var results = jdbcTemplate.query("select name from users", NAME_ROW_MAPPER);

        assertThat(results).containsExactly("gugu", "dahye");
    }

    @DisplayName("query가 끝나면 ResultSet, PreparedStatement, Connection을 닫는다")
    @Test
    void queryClosesResources() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        jdbcTemplate.query("select name from users", NAME_ROW_MAPPER);

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @DisplayName("queryForObject는 결과가 한 건이면 값을 담은 Optional을 반환한다")
    @Test
    void queryForObject() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("gugu");

        final var result = jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L);

        assertThat(result).contains("gugu");
        verify(preparedStatement).setObject(1, 1L);
    }

    @DisplayName("queryForObject는 결과가 없으면 빈 Optional을 반환한다")
    @Test
    void queryForObjectWhenEmpty() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        final var result = jdbcTemplate.queryForObject("select name from users where id = ?", NAME_ROW_MAPPER, 1L);

        assertThat(result).isEmpty();
    }

    @DisplayName("queryForObject는 결과가 두 건 이상이면 예외가 발생한다")
    @Test
    void queryForObjectWhenMultipleResults() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("gugu", "dahye");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select name from users", NAME_ROW_MAPPER))
                .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("SQLException이 발생하면 DataAccessException으로 감싸고 자원을 닫는다")
    @Test
    void wrapSQLException() throws SQLException {
        final var cause = new SQLException("DB 에러");
        when(preparedStatement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update("delete from users"))
                .isInstanceOf(DataAccessException.class)
                .hasCause(cause);
        verify(preparedStatement).close();
        verify(connection).close();
    }
}
