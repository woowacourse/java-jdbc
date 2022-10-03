package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    final Connection connection = mock(Connection.class);
    final PreparedStatement preparedStatement = mock(PreparedStatement.class);

    @BeforeEach
    void init() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void update() throws SQLException {
        // given
        final String sql = "update users set account = gugu, password = pw, email = gugu@woowa.net where id = 1";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        // when
        jdbcTemplate.update(sql);

        // then
        assertAll(() -> {
            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(sql);
            verify(preparedStatement, times(1)).executeUpdate();
            verify(connection, times(1)).close();
            verify(preparedStatement, times(1)).close();
        });
    }

    @Test
    void query_actionValidation() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users";
        final RowMapper<String> classRowMapper = rs -> "result";

        final ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // when
        final List<String> result = jdbcTemplate.query(sql, classRowMapper);

        // then
        assertAll(() -> {
            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(sql);
            verify(preparedStatement, times(1)).executeQuery();
            verify(resultSet, times(1)).close();
            verify(connection, times(1)).close();
            verify(preparedStatement, times(1)).close();
        });
    }

    @Test
    void query_stateValidation() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users";
        final RowMapper<String> classRowMapper = rs -> "result";

        final ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);

        // when
        final List<String> result = jdbcTemplate.query(sql, classRowMapper);

        // then
        assertThat(result).hasSize(3)
                .containsExactly("result", "result", "result");
    }

    @Test
    void queryForObject() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users where id = ?";
        final RowMapper<String> classRowMapper = rs -> "result";

        final ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        // when
        final String result = jdbcTemplate.queryForObject(sql, classRowMapper, 1);

        // then
        assertAll(() -> {
            verify(dataSource, times(1)).getConnection();
            verify(connection, times(1)).prepareStatement(sql);
            verify(preparedStatement, times(1)).executeQuery();
            verify(resultSet, times(1)).close();
            verify(connection, times(1)).close();
            verify(preparedStatement, times(1)).close();
        });
    }

    @Test
    void queryForObject_EmptyResultException() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users where id = ?";
        final RowMapper<String> classRowMapper = rs -> "result";

        final ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, classRowMapper, 1))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Empty result");
    }

    @Test
    void queryForObject_IncorrectResultException() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users where id = ?";
        final RowMapper<String> classRowMapper = rs -> "result";

        final ResultSet resultSet = mock(ResultSet.class);

        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, classRowMapper, 1))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Incorrect result size, expected : 1, actual : 2");
    }
}