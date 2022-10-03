package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.stream.Collectors;
import nextstep.config.DataSourceConfig;
import nextstep.config.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    }

    @AfterEach
    void reset() {
        final String findQuery = "select id, account, password, email from users";
        final List<User> users = jdbcTemplate.query(findQuery, UserMapper.getInstance());

        final String deleteQuery = "delete from users where id = ?";
        users.forEach(user -> jdbcTemplate.update(deleteQuery, user.getId()));
    }

    @Test
    void update() {
        insertUser(new User("gugu", "password", "hkkang@woowahan.com"));

        final String findQuery = "select id, account, password, email from users where id = ?";
        final User findUser = jdbcTemplate.queryForObject(findQuery, UserMapper.getInstance(), 1L);
        assertThat(findUser).isNotNull();
    }

    @Test
    void query() {
        insertUser(new User("gugu", "password", "hkkang@woowahan.com"));
        insertUser(new User("ash", "ash123", "ash@gmail.com"));

        final String findQuery = "select id, account, password, email from users";
        final List<User> users = jdbcTemplate.query(findQuery, UserMapper.getInstance());
        assertThat(users.stream()
                    .map(User::getAccount)
                    .collect(Collectors.toList()))
                .hasSize(2)
                .contains("gugu", "ash");
    }

    @Test
    void queryForObject() {
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        insertUser(user);

        final String findQuery = "select id, account, password, email from users where account = ?";
        final User findUser = jdbcTemplate.queryForObject(findQuery, UserMapper.getInstance(), "gugu");
        assertAll(
                () -> assertThat(findUser.getAccount()).isEqualTo(user.getAccount()),
                () -> assertThat(findUser.getPassword()).isEqualTo(user.getPassword()),
                () -> assertThat(findUser.getEmail()).isEqualTo(user.getEmail())
        );
    }

    private void insertUser(final User user) {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(),
                user.getPassword(),
                user.getEmail());
    }
}