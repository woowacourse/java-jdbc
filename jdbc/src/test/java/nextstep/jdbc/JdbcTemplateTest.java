package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateTest() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    }

    private RowMapper<User> userRowMapper = (rs -> new User(
        rs.getLong("id"),
        rs.getString("account"),
        rs.getString("password"),
        rs.getString("email")
    ));

    @BeforeEach
    void setUp() {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "gugu", "password", "email");
    }

    @Test
    void update() {
        String sql = "update users set password = ?, email = ? where account = ? ";
        jdbcTemplate.update(sql, "password99", "email2", "gugu");

        String querySql = "select id, account, password, email from users where account = ?";
        User updateUser = jdbcTemplate.queryForObject(querySql, userRowMapper, "gugu");

        assertThat(updateUser.getPassword()).isEqualTo("password99");
    }

    @Test
    void findById() {
        String sql = "select id, account, password, email from users where id = ?";
        User findUser = jdbcTemplate.queryForObject(sql, userRowMapper, 1L);

        assertThat(findUser.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        String sql = "select id, account, password, email from users where account = ?";
        User findUser = jdbcTemplate.queryForObject(sql, userRowMapper, "gugu");

        assertThat(findUser.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findAll() {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "abc", "password", "abc@abc.com");

        String findSql = "select id, account, password, email from users";
        List<User> users =  jdbcTemplate.query(findSql, userRowMapper);

        assertThat(users.size()).isEqualTo(2);
    }

}