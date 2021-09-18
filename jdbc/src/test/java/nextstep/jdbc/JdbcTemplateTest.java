package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JdbcTemplateTest {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplateTest.class);
    private static final RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private User user;

    @BeforeEach
    void setup() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate() {
            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        };

        user = new User(1L, "account", "password", "email@email.com");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    @AfterEach
    void cleanUp() {
        final String sql = "drop table users";
        jdbcTemplate.update(sql);
    }

    @Test
    void insert() {
        final User newUser = new User("newAccount", "newPassword", "newEmail@email.com");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, newUser.getAccount(), newUser.getPassword(), newUser.getEmail());

        final String selectSql = "select id, account, password, email from users where id = ?";
        User dbUser = Optional.ofNullable(jdbcTemplate.queryForObject(selectSql, userRowMapper, 2L))
                .orElseThrow(IllegalArgumentException::new);
        assertThat(dbUser.getAccount()).isEqualTo(newUser.getAccount());
        assertThat(dbUser.getPassword()).isEqualTo(newUser.getPassword());
        assertThat(dbUser.getEmail()).isEqualTo(newUser.getEmail());
    }

    @Test
    void update() {
        final String sql = "update users set password = ? where id = ?";
        final String newPassword = "newPassword";

        jdbcTemplate.update(sql, newPassword, user.getId());

        final String selectSql = "select id, account, password, email from users where id = ?";
        User dbUser = Optional.ofNullable(jdbcTemplate.queryForObject(selectSql, userRowMapper, 1L))
                .orElseThrow(IllegalArgumentException::new);
        assertThat(dbUser.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void findById() {
        final String sql = "select id, account, password, email from users where id = ?";
        final Long id = 1L;

        final User dbUser = Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, id))
                .orElseThrow(IllegalArgumentException::new);

        assertThat(dbUser.getId()).isEqualTo(user.getId());
        assertThat(dbUser.getAccount()).isEqualTo(user.getAccount());
        assertThat(dbUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(dbUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findByAccount() {
        final String sql = "select id, account, password, email from users where account = ?";
        final String account = "account";

        final User dbUser = Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, account))
                .orElseThrow(IllegalArgumentException::new);

        assertThat(dbUser.getId()).isEqualTo(user.getId());
        assertThat(dbUser.getAccount()).isEqualTo(user.getAccount());
        assertThat(dbUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(dbUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findAll() {
        List<User> users = new ArrayList<>();
        users.add(user);
        final User user2 = new User("account2", "password2", "email2@email.com");
        users.add(user2);
        final User user3 = new User("account3", "password3", "email3@email.com");
        users.add(user3);
        final String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, user2.getAccount(), user2.getPassword(), user2.getEmail());
        jdbcTemplate.update(insertSql, user3.getAccount(), user3.getPassword(), user3.getEmail());
        final String sql = "select id, account, password, email from users";

        List<User> dbUsers = jdbcTemplate.query(sql, userRowMapper);

        assertThat(dbUsers.size()).isEqualTo(users.size());
    }
}
