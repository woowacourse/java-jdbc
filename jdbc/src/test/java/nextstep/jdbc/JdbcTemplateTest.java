package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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

    private JdbcTemplate jdbcTemplate;
    private PreparedStatement preparedStatement;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        this.preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void update() throws SQLException {
        String sql = "insert into users (name) values (?)";
        User user = new User("name");

        jdbcTemplate.update(sql, user.getName());

        verify(preparedStatement).executeUpdate();
    }

    @Test
    void query() throws SQLException {
        String sql = "select * from users";
        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        List<User> users = jdbcTemplate.query(sql, rs -> new User("name"));

        assertThat(users).hasSize(1);
    }

    @Test
    void queryForObject() throws SQLException {
        String sql = "select * from users where name = ?";
        ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        User user = jdbcTemplate.queryForObject(sql, rs -> new User("name"), "name");

        assertThat(user.getName()).isEqualTo("name");
    }

    private static class User {

        private final String name;

        public User(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
