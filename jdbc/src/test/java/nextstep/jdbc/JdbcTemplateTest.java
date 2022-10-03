package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.resultset.RowMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource;
    private final Connection connection;
    private final PreparedStatement statement;
    private final ResultSet resultSet;
    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateTest() {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.statement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);

        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("update query 수행을 확인한다.")
    @Test
    void update() throws SQLException {
        // given
        final var sql = "";
        final var args = new Object[]{"arg1", 2};

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        // when
        jdbcTemplate.update(sql, args);

        // then
        assertAll(
                () -> verify(statement).setObject(1, args[0]),
                () -> verify(statement).setObject(2, args[1]),
                () -> verify(statement).executeUpdate(),
                () -> verify(connection).close(),
                () -> verify(statement).close()
        );
    }

    @DisplayName("select query 수행을 확인한다.")
    @Test
    void query() throws SQLException {
        // given
        final var sql = "";
        final RowMapper<String> rowMapper = (rs) -> "result object";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        // when
        final var actual = jdbcTemplate.query(sql, rowMapper);

        // then
        assertAll(
                () -> assertThat(actual).containsOnly("result object", "result object"),
                () -> verify(connection).close(),
                () -> verify(statement).close(),
                () -> verify(resultSet).close()
        );
    }

    @DisplayName("단일결과 select query 수행을 확인한다.")
    @Test
    void queryForObject() throws SQLException {
        // given
        final var sql = "";
        final RowMapper<String> rowMapper = (rs) -> "result object";
        final var args = new Object[]{"arg1", 2};

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        // when
        final var actual = jdbcTemplate.queryForObject(sql, rowMapper, args);

        // then
        assertAll(
                () -> assertThat(actual).isEqualTo("result object"),
                () -> verify(connection).close(),
                () -> verify(statement).close(),
                () -> verify(resultSet).close()
        );
    }

    @DisplayName("단일결과 select query 수행 시 결과가 비어있으면 예외를 던진다.")
    @Test
    void queryForObject_empty() throws SQLException {
        // given
        final var sql = "";
        final RowMapper<String> rowMapper = (rs) -> "result object";
        final var args = new Object[]{"arg1", 2};

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, args))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("A result is empty.");
    }

    @DisplayName("단일결과 select query 수행 시 결과가 1개 초과면 예외를 던진다.")
    @Test
    void queryForObject_over() throws SQLException {
        // given
        final var sql = "";
        final RowMapper<String> rowMapper = (rs) -> "result object";
        final var args = new Object[]{"arg1", 2};

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, args))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("A result is over one.");
    }
}
