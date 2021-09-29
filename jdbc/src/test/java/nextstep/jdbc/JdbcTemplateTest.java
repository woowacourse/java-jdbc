package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import nextstep.jdbc.app.DataSourceConfig;
import nextstep.jdbc.app.DatabasePopulatingUtils;
import nextstep.jdbc.app.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DatabasePopulatingUtils.execute(DataSourceConfig.getInstance(), "create-schema.sql");
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, "dani", "dani", "dani@woowahan.com");
    }

    @DisplayName("수정")
    @Test
    void update() {
        // given
        String updateSql = "update users set password = ? where id = ?";

        User expected = new User(1L, "dani", "dada", "dani@woowahan.com");

        // when
        jdbcTemplate.update(updateSql, "dada", 1);

        // then
        String selectSql = "select id, account, password, email from users where id = ?";
        User actual = jdbcTemplate.queryForObject(selectSql, this::createUser, 1);

        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @DisplayName("다중 조회")
    @Test
    void query() {
        // given
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, "pika", "pika", "pika@woowahan.com");

        String selectSql = "select id, account, password, email from users";

        // when
        List<User> users = jdbcTemplate.query(selectSql, this::createUser);

        // then
        assertThat(users).hasSize(2);
    }

    @DisplayName("단일 조회")
    @Test
    void queryForObject() {
        // given
        String selectSql = "select id, account, password, email from users where id = ?";

        User expected = new User(1L, "dani", "dani", "dani@woowahan.com");

        // when
        User actual = jdbcTemplate.queryForObject(selectSql, this::createUser, 1);

        // then
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    private User createUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"));
    }

    @AfterEach
    void tearDown() {
        DatabasePopulatingUtils.execute(DataSourceConfig.getInstance(), "drop-schema.sql");
    }
}
