package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.jdbc.utils.TestDataSourceConfig;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final String INIT_SQL = """
            create table if not exists users (
                id bigint auto_increment,
                password varchar(100) not null,
                primary key(id)
            );
            """;
    private static final String INSERT_USERS_SQL =
            "insert into users (password) values ('1111'), ('2222');";
    private static final String TRUNCATE_SQL =
            "TRUNCATE TABLE users RESTART IDENTITY;";
    private static final RowMapper<User> ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("password"));

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        TestDataSourceConfig.execute(INIT_SQL);
        TestDataSourceConfig.execute(INSERT_USERS_SQL);

        DataSource dataSource = TestDataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() {
        TestDataSourceConfig.execute(TRUNCATE_SQL);
    }

    @Test
    void queryForObjectTest() {
        String sql = "select * from users where id = ?;";
        long id = 1L;

        Optional<User> user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);

        assertThat(user).contains(new User(id, "1111"));
    }

    @Test
    void queryForObjectTest_whenUsingPreparedStatementSetter() {
        String sql = "select * from users where id = ?;";
        PreparedStatementSetter preparedStatementSetter = preparedStatement ->
            preparedStatement.setLong(1, 1L);

        Optional<User> user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, preparedStatementSetter);

        assertThat(user).contains(new User(1L, "1111"));
    }

    @Test
    void queryForObjectTest_whenNotExist_returnEmpty() {
        String sql = "select * from users where id = ?;";
        long id = 10000L;

        Optional<User> user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, id);

        assertThat(user).isEmpty();
    }

    @Test
    void queryForObjectTest_whenExistOver2_throwException() {
        String sql = "select * from users;";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, ROW_MAPPER))
                .isInstanceOf(NotSingleResultDataAccessException.class);
    }

    @Test
    void queryTest() {
        String sql = "select * from users;";

        List<User> users = jdbcTemplate.query(sql, ROW_MAPPER);

        assertThat(users).containsOnly(new User(1L, "1111"), new User(2L, "2222"));
    }

    @Test
    void queryTest_whenUsingParameter() {
        String sql = "select * from users where id = ?;";
        long id = 1L;

        List<User> users = jdbcTemplate.query(sql, ROW_MAPPER, id);

        assertThat(users).containsOnly(new User(1L, "1111"));
    }

    @Test
    void queryTest_whenUsingPreparedStatementSetter() {
        String sql = "select * from users where id = ?;";
        PreparedStatementSetter preparedStatementSetter = preparedStatement ->
                preparedStatement.setLong(1, 1L);

        List<User> users = jdbcTemplate.query(sql, ROW_MAPPER, preparedStatementSetter);

        assertThat(users).containsOnly(new User(1L, "1111"));
    }

    @Test
    void updateTest() {
        String sql = "insert into users (password) values ('3333');";

        jdbcTemplate.update(sql);

        List<User> users = jdbcTemplate.query("select * from users;", ROW_MAPPER);
        assertThat(users).hasSize(3);
    }

    @Test
    void updateTest_whenUsingParameter() {
        String sql = "insert into users (password) values (?);";
        String password = "3333";

        jdbcTemplate.update(sql, password);

        List<User> users = jdbcTemplate.query("select * from users;", ROW_MAPPER);
        assertThat(users).hasSize(3);
    }

    @Test
    void updateTest_whenUsingPreparedStatementSetter() {
        String sql = "insert into users (password) values (?);";
        PreparedStatementSetter preparedStatementSetter = preparedStatement ->
                preparedStatement.setString(1, "3333");

        jdbcTemplate.update(sql, preparedStatementSetter);

        List<User> users = jdbcTemplate.query("select * from users;", ROW_MAPPER);
        assertThat(users).hasSize(3);
    }

    private static class User {

        private final long id;
        private final String password;

        public User(long id, String password) {
            this.id = id;
            this.password = password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            User user = (User) o;
            return id == user.id && Objects.equals(password, user.password);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, password);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                    .add("id=" + id)
                    .add("password='" + password + "'")
                    .toString();
        }
    }
}
