package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.EmptyResultException;
import nextstep.jdbc.exception.IncorrectDataSizeException;
import nextstep.jdbc.resultset.RowMapper;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() throws SQLException {
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(statement);
    }

    @DisplayName("update query 수행을 확인한다.")
    @Test
    void update() throws SQLException {
        // given
        final var sql = "";
        final var args = new Object[]{"arg1", 2};

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

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true).willReturn(true).willReturn(false);

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

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true).willReturn(false);

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

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, args))
                .isInstanceOf(EmptyResultException.class);
    }

    @DisplayName("단일결과 select query 수행 시 결과가 1개 초과면 예외를 던진다.")
    @Test
    void queryForObject_over() throws SQLException {
        // given
        final var sql = "";
        final RowMapper<String> rowMapper = (rs) -> "result object";
        final var args = new Object[]{"arg1", 2};

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true).willReturn(true).willReturn(false);

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, args))
                .isInstanceOf(IncorrectDataSizeException.class);
    }
}
