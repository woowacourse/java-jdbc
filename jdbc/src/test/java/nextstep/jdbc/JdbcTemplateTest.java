package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    private JdbcTemplate jdbcTemplate;
    private PreparedStatement preparedStatement;
    private Connection connection;
    private ResultSet resultSet;

    @BeforeEach
    void setup() throws SQLException {
        preparedStatement = mock(PreparedStatement.class);
        connection = mock(Connection.class);
        resultSet = mock(ResultSet.class);
        final DataSource dataSource = mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(dataSource.getConnection()).thenReturn(connection);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    void update() throws SQLException {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        jdbcTemplate.update(sql, user.getAccount(),
                user.getPassword(),
                user.getEmail());

        verify(preparedStatement).setObject(1, "gugu");
        verify(preparedStatement).setObject(2, "password");
        verify(preparedStatement).setObject(3, "hkkang@woowahan.com");
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).close();
    }

    @Test
    void query() throws SQLException {
        // given
        final User gugu = new User("gugu", "password", "hkkang@woowahan.com");
        final User ash = new User("ash", "ash123", "ash@gmail.com");
        final RowMapper<User> rowMapper = mock(RowMapper.class);
        final String sql = "select id, account, password, email from users";

        // when
        when(resultSet.next()).thenReturn(true, true, false);
        when(rowMapper.mapRow(resultSet, 0)).thenReturn(gugu);
        when(rowMapper.mapRow(resultSet, 1)).thenReturn(ash);
        final List<User> users = jdbcTemplate.query(sql, rowMapper);

        // then
        assertThat(users).extracting("account")
                .contains("gugu", "ash");
        verify(resultSet, times(3)).next();
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
    }

    @Test
    void queryForObject() throws SQLException {
        final User gugu = new User("gugu", "password", "hkkang@woowahan.com");
        final RowMapper<User> rowMapper = mock(RowMapper.class);
        final String sql = "select id, account, password, email from users where account = ?";

        when(resultSet.next()).thenReturn(true, false);
        when(rowMapper.mapRow(resultSet, 0)).thenReturn(gugu);
        final User user = jdbcTemplate.queryForObject(sql, rowMapper);

        assertThat(user.getAccount()).isEqualTo("gugu");
        verify(resultSet, times(2)).next();
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
    }
}
