package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {
    private final RowMapper<User> userMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email"));

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private User baseUser;

    @BeforeEach
    void setup() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);

        baseUser = new User(1L, "account", "password", "test@email.com");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, baseUser.getAccount(), baseUser.getPassword(), baseUser.getEmail());
    }

    @AfterEach
    void clean() {
        final String sql = "drop table users";
        jdbcTemplate.update(sql);
    }

    @DisplayName("update")
    @Test
    void passwordChange() {
        final String sql = "update users set password = ? where id = ?";
        final String newPassword = "newPassword";

        jdbcTemplate.update(sql, newPassword, baseUser.getId());

        User account = findUserByAccount("account");
        assertThat(account).isEqualTo(new User(1L, "account", "newPassword", "test@email.com"));
    }

    @DisplayName("query for object")
    @Test
    void find() {
        findUserByAccount("account");
    }

    @DisplayName("query for object but many value")
    @Test
    void findDuplicationObject() {
        User baseUser2 = new User("account", "password", "test@email.com");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, baseUser2.getAccount(), baseUser2.getPassword(), baseUser2.getEmail());

        assertThatThrownBy(() -> findUserByAccount("account")).isInstanceOf(DataException.class);
    }

    @DisplayName("query for object but 0 result")
    @Test
    void findInvalidUser() {
        assertThatThrownBy(() -> findUserByAccount("invalidAccount")).isInstanceOf(DataException.class);
    }

    @DisplayName("query objects")
    @Test
    void findObjects() {
        User baseUser2 = new User("account", "password", "test@email.com");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, baseUser2.getAccount(), baseUser2.getPassword(), baseUser2.getEmail());
        final String findAllSql = "select * from users";

        assertThat(jdbcTemplate.query(findAllSql, userMapper)).hasSize(2);
    }

    User findUserByAccount(String account) {
        return jdbcTemplate.queryForObject("select * from users where account = ?", userMapper, account);
    }


}