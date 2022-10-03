package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
    }

    @Test
    void insert문을_실행한다() throws SQLException {
        // given
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";

        // when
        jdbcTemplate.execute(sql, "account", "password", "email");

        // then
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update문을_실행한다() throws SQLException {
        // given
        final var sql = "update users set account = ? ,password = ? ,email = ? where id = ?";

        // when
        jdbcTemplate.execute(sql, "account", "password", "email", 1);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void 다수의_요소를_조회한다() throws SQLException {
        // given
        final var sql = "select id, account, password, email from users";
        final RowMapper<Object> rowMapper = rs -> new Object();

        // when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        List<Object> result = jdbcTemplate.query(sql, rowMapper);

        // then
        verify(resultSet).close();
        verify(connection).close();
        verify(preparedStatement).close();
        assertThat(result).hasSize(2);
    }

    @Test
    void 하나의_요소를_조회한다() throws SQLException {
        // given
        final var sql = "select id, account, password, email from users where id = ?";
        final Object object = new Object();
        final RowMapper<Object> rowMapper = rs -> object;

        // when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        Object result = jdbcTemplate.queryForObject(sql, rowMapper, 1);

        // then
        verify(resultSet).close();
        verify(connection).close();
        verify(preparedStatement).close();
        assertThat(result).isEqualTo(object);
    }
}
