package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import nextstep.support.DataSourceConfig;
import nextstep.support.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<TestUser> TEST_USER_ROW_MAPPER = resultSet -> new TestUser(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

        String dropSql = "drop table if exists test_users;";
        jdbcTemplate.update(dropSql);

        String createTableSql = "create table test_users (\n"
                + "    id bigint auto_increment,\n"
                + "    account varchar(100) not null,\n"
                + "    password varchar(100) not null,\n"
                + "    email varchar(100) not null,\n"
                + "    primary key(id)\n"
                + ");";
        jdbcTemplate.update(createTableSql);
    }

    @Test
    void update() {
        TestUser user = new TestUser("parang", "pass", "parang@email.com");
        String sql = "insert into test_users (account, password, email) values (?, ?, ?)";

        int result = jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());

        assertThat(result).isEqualTo(1);
    }

    @Test
    void queryForObject() {
        TestUser user = new TestUser("parang", "pass", "parang@email.com");
        String insertSql = "insert into test_users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, user.getAccount(), user.getPassword(), user.getEmail());

        String findSql = "select id, account, password, email from test_users where account = ?";
        TestUser findUser = jdbcTemplate.queryForObject(findSql, TEST_USER_ROW_MAPPER, "parang");

        assertAll(
                () -> {
                    assertThat(findUser.getAccount()).isEqualTo("parang");
                    assertThat(findUser.getPassword()).isEqualTo("pass");
                    assertThat(findUser.getEmail()).isEqualTo("parang@email.com");
                }
        );
    }

    @Test
    void query() {
        String insertSql = "insert into test_users (account, password, email) values (?, ?, ?)";
        TestUser user1 = new TestUser("parang1", "pass", "parang@email.com");
        TestUser user2 = new TestUser("parang2", "pass", "parang@email.com");

        jdbcTemplate.update(insertSql, user1.getAccount(), user1.getPassword(), user1.getEmail());
        jdbcTemplate.update(insertSql, user2.getAccount(), user2.getPassword(), user2.getEmail());

        String findSql = "select id, account, password, email from test_users";
        List<TestUser> findUsers = jdbcTemplate.query(findSql, TEST_USER_ROW_MAPPER);

        assertThat(findUsers).hasSize(2);
        assertThat(findUsers).extracting("account")
                .contains("parang1", "parang2");
    }
}
