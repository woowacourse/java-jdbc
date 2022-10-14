package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        given(dataSource.getConnection()).willReturn(connection);
    }

    @Test
    void update() throws SQLException {
        // given
        final String sql = "update users set account = gugu, password = pw, email = gugu@woowa.net where id = 1";
        given(connection.prepareStatement(sql)).willReturn(preparedStatement);

        // when
        jdbcTemplate.update(sql);

        // then
        assertAll(() -> {
            verify(dataSource).getConnection();
            verify(connection).prepareStatement(sql);
            verify(preparedStatement).executeUpdate();
            verify(connection).close();
            verify(preparedStatement).close();
        });
    }

    @Test
    void query_actionValidation() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users";
        final RowMapper<String> classRowMapper = rs -> "result";

        final ResultSet resultSet = mock(ResultSet.class);

        given(connection.prepareStatement(sql)).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        // when
        final List<String> result = jdbcTemplate.query(sql, classRowMapper);

        // then
        assertAll(() -> {
            verify(dataSource).getConnection();
            verify(connection).prepareStatement(sql);
            verify(preparedStatement).executeQuery();
            verify(resultSet).close();
            verify(connection).close();
            verify(preparedStatement).close();
        });
    }

    @Test
    void query_stateValidation() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users";
        final RowMapper<String> classRowMapper = rs -> "result";

        final ResultSet resultSet = mock(ResultSet.class);

        given(connection.prepareStatement(sql)).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, true, false);

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

        given(connection.prepareStatement(sql)).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, false);

        // when
        final String result = jdbcTemplate.queryForObject(sql, classRowMapper, 1);

        // then
        assertAll(() -> {
            verify(dataSource).getConnection();
            verify(connection).prepareStatement(sql);
            verify(preparedStatement).executeQuery();
            verify(resultSet).close();
            verify(connection).close();
            verify(preparedStatement).close();
        });
    }

    @Test
    void queryForObject_EmptyResultException() throws SQLException {
        // given
        final String sql = "select id, account, password, email from users where id = ?";
        final RowMapper<String> classRowMapper = rs -> "result";

        final ResultSet resultSet = mock(ResultSet.class);

        given(connection.prepareStatement(sql)).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(false);

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

        given(connection.prepareStatement(sql)).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, classRowMapper, 1))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("Incorrect result size, expected : 1, actual : 2");
    }
}
