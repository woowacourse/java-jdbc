package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
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

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final Connection connection;
    private final PreparedStatement preparedStatement;
    private final ResultSet resultSet;

    JdbcTemplateTest() {
        this.dataSource = mock(DataSource.class);
        this.connection = mock(Connection.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @BeforeEach
    void setUp() throws SQLException {
        given(dataSource.getConnection()).willReturn(connection);
    }

    @Test
    void insert문을_실행한다() throws SQLException {
        // given
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        given(connection.prepareStatement(sql)).willReturn(preparedStatement);

        // when
        jdbcTemplate.update(sql, "account", "password", "email");

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, "account"),
                () -> verify(preparedStatement).setObject(2, "password"),
                () -> verify(preparedStatement).setObject(3, "email"),
                () -> verify(preparedStatement).executeUpdate(),
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close()
        );
    }

    @Test
    void update문을_실행한다() throws SQLException {
        // given
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        given(connection.prepareStatement(sql)).willReturn(preparedStatement);

        // when
        jdbcTemplate.update(sql, "account", "password", "email", 1);

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, "account"),
                () -> verify(preparedStatement).setObject(2, "password"),
                () -> verify(preparedStatement).setObject(3, "email"),
                () -> verify(preparedStatement).setObject(4, 1),
                () -> verify(preparedStatement).executeUpdate(),
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close()
        );
    }

    @Test
    void 다수의_요소를_조회한다() throws SQLException {
        // given
        final var sql = "select id, account, password, email from users";
        final RowMapper<Object> rowMapper = rs -> new Object();

        given(connection.prepareStatement(sql)).willReturn(preparedStatement);

        // when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        List<Object> result = jdbcTemplate.query(sql, rowMapper);

        // then
        assertAll(
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(resultSet).close(),
                () -> assertThat(result).hasSize(2)
        );
    }

    @Test
    void 하나의_요소를_조회한다() throws SQLException {
        // given
        final var sql = "select id, account, password, email from users where id = ?";
        final Object object = new Object();
        final RowMapper<Object> rowMapper = rs -> object;

        given(connection.prepareStatement(sql)).willReturn(preparedStatement);

        // when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        Object result = jdbcTemplate.queryForObject(sql, rowMapper, 1);

        // then
        assertAll(
                () -> verify(preparedStatement).setObject(1, 1),
                () -> assertThat(result).isEqualTo(object),
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(resultSet).close()
        );
    }

    @Test
    void 하나의_요소_조회_시_조회에_실패하면_예외를_던진다() throws SQLException {
        // given
        final var sql = "select id, account, password, email from users where id = ?";
        final Object object = new Object();
        final RowMapper<Object> rowMapper = rs -> object;

        given(connection.prepareStatement(sql)).willReturn(preparedStatement);

        // when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // then
        assertAll(
                () -> assertThatThrownBy(
                        () -> jdbcTemplate.queryForObject(sql, rowMapper, 1)
                ).isInstanceOf(DataAccessException.class),
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(resultSet).close()
        );
    }

    @Test
    void 하나의_요소_조회_시_두개_이상의_요소가_조회되면_예외를_던진다() throws SQLException {
        // given
        final var sql = "select id, account, password, email from users where id = ?";
        final RowMapper<Object> rowMapper = rs -> new Object();

        given(connection.prepareStatement(sql)).willReturn(preparedStatement);

        // when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        // then
        assertAll(
                () -> assertThatThrownBy(
                        () -> jdbcTemplate.queryForObject(sql, rowMapper, 1)
                ).isInstanceOf(DataAccessException.class),
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(resultSet).close()
        );
    }
}
