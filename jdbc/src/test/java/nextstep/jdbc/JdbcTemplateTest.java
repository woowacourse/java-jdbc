package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = Mockito.mock(DataSource.class);
        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Nested
    @DisplayName("update 메서드는")
    class Update {

        @Test
        @DisplayName("INSERT 쿼리를 처리할 수 있다.")
        void success_insert_only() throws SQLException {
            User user = new User("leo", "password");
            String sql = String.format(
                "insert into users (account, password) values (%s, %s)",
                user.getAccount(), user.getPassword());

            when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
            jdbcTemplate.update(sql, connection);

            Mockito.verify(preparedStatement).executeUpdate();
        }

        @Test
        @DisplayName("파라미터가 있는 INSERT 쿼리를 처리할 수 있다.")
        void success_insert_parameters() throws SQLException {
            User user = new User("leo", "password");

            String sql = "insert into users (account, password) values (?, ?)";
            when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
            jdbcTemplate.update(sql, connection, user.getAccount(), user.getPassword());

            Mockito.verify(preparedStatement).setObject(1, "leo");
            Mockito.verify(preparedStatement).setObject(2, "password");
        }
    }

    @Nested
    @DisplayName("queryForObject 메서드는")
    class QueryForObject {

        private ResultSet resultSet;

        @BeforeEach
        void setUp() {
            resultSet = Mockito.mock(ResultSet.class);
        }

        @Test
        @DisplayName("결과행이 1개인 SELECT 쿼리를 처리할 수 있다.")
        void success_select_only() throws SQLException {
            String sql = "select * from users where account = leo";
            RowMapper<User> rowMapper = (rs, rowNum) ->
                new User(rs.getString(1), rs.getString(2));

            when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString(1)).thenReturn("leo");
            when(resultSet.getString(2)).thenReturn("password");
            User actual = jdbcTemplate.queryForObject(sql, rowMapper, connection);

            User expected = new User("leo", "password");
            assertThat(actual).usingRecursiveComparison()
                .isEqualTo(expected);
        }

        @Test
        @DisplayName("파라미터가 있는 결과행이 1개인 SELECT 쿼리를 처리할 수 있다.")
        void success_select_parameters() throws SQLException {
            String sql = "select * from users where account = ?";
            RowMapper<User> rowMapper = (rs, rowNum) ->
                new User(rs.getString(1), rs.getString(2));

            when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getString(1)).thenReturn("leo");
            when(resultSet.getString(2)).thenReturn("password");
            jdbcTemplate.queryForObject(sql, rowMapper, connection, "leo");

            Mockito.verify(preparedStatement).setObject(1, "leo");
        }
    }

    @Nested
    @DisplayName("query 메서드는")
    class Query {

        private ResultSet resultSet;

        @BeforeEach
        void setUp() {
            resultSet = Mockito.mock(ResultSet.class);
        }

        @Test
        @DisplayName("결과행이 1개 이상인 SELECT 쿼리를 처리할 수 있다.")
        void success_select_only() throws SQLException {
            String sql = "select * from users";
            RowMapper<User> rowMapper = (rs, rowNum) ->
                new User(rs.getString(1), rs.getString(2));

            when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getString(1)).thenReturn("leo", "leo2");
            when(resultSet.getString(2)).thenReturn("password");
            List<User> users = jdbcTemplate.query(sql, rowMapper, connection);

            assertThat(users).hasSize(2);
        }
    }

    private static class User {

        private final String account;
        private final String password;

        public User(String account, String password) {
            this.account = account;
            this.password = password;
        }

        public String getAccount() {
            return account;
        }

        public String getPassword() {
            return password;
        }
    }
}
