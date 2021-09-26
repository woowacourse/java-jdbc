package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private RowMapper<User> userRowMapper;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userRowMapper = userRowMapper = rs -> new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"));

        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "bepoz", "1234", "bepoz@jdbc.ocm");
    }

    @Test
    @DisplayName("update Test")
    void updateCountTest() {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        int rowCount = jdbcTemplate.update(sql, "gump", "1234", "gump@jdbc.com");

        assertThat(rowCount).isEqualTo(1);
    }

    @Test
    @DisplayName("queryForObject Test")
    public void queryforObjectTest() {
        String sql = "select id, account, password, email from users where id = 1";
        User user = jdbcTemplate.queryForObject(sql, userRowMapper);

        assertThat(user).isEqualTo(new User(1L, "bepoz", "1234", "bepoz@jdbc.com"));
    }

    @Test
    @DisplayName("query Test")
    public void queryTest() {
        String sql = "select id, account, password, email from users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);

        assertThat(users).containsExactlyInAnyOrder(new User(1L, "bepoz", "1234", "bepoz@jdbc.com"));
    }
}
