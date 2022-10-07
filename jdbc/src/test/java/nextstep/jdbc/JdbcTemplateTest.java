package nextstep.jdbc;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<TestUser> ROW_MAPPER = getMapper();

    private JdbcTemplate jdbcTemplate;

    private static RowMapper<TestUser> getMapper() {
        return (rs, rowNum) -> new TestUser(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4));
    }

    @BeforeEach
    void setup() {
        TestDatabasePopulatorUtils.execute(TestDataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(TestDataSourceConfig.getInstance());
    }

    @Test
    void insert() {
        // given
        final var insertQuery = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertQuery, "gugu", "password", "hkkang@woowahan.com");

        // when, then
        final var selectQuery = "select id, account, password, email from users";
        List<TestUser> users = jdbcTemplate.query(selectQuery, ROW_MAPPER);

        assertAll(() -> assertThat(users).hasSize(1), () -> assertThat(users.get(0).getAccount()).isEqualTo("gugu"),
            () -> assertThat(users.get(0).checkPassword("password")).isTrue(),
            () -> assertThat(users.get(0).getEmail()).isEqualTo("hkkang@woowahan.com"));
    }

    @Test
    void update() {
        // given
        final var insertQuery = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertQuery, "gugu", "password", "hkkang@woowahan.com");

        // when
        final var sql = "update users set account = ?, password = ?, email = ? where id = ?";
        
        jdbcTemplate.update(sql, "gugu", "new password", "new email@woowahan.com", 1);

        // then
        final var selectQuery = "select id, account, password, email from users";
        List<TestUser> users = jdbcTemplate.query(selectQuery, ROW_MAPPER);

        assertAll(() -> assertThat(users).hasSize(1), () -> assertThat(users.get(0).getAccount()).isEqualTo("gugu"),
            () -> assertThat(users.get(0).checkPassword("password")).isFalse(),
            () -> assertThat(users.get(0).checkPassword("new password")).isTrue(),
            () -> assertThat(users.get(0).getEmail()).isEqualTo("new email@woowahan.com"));
    }

    @Test
    void queryForObject() {
        // given
        final var insertQuery = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertQuery, "gugu", "password", "hkkang@woowahan.com");

        // when, then
        final var sql = "select id, account, password, email from users where account = ?";
        TestUser user = jdbcTemplate.queryForObject(sql, ROW_MAPPER, "gugu");

        assertAll(() -> assertThat(user.getAccount()).isEqualTo("gugu"),
            () -> assertThat(user.checkPassword("password")).isTrue(),
            () -> assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com"));
    }

    @Test
    void query() {
        // given
        final var insertQuery1 = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertQuery1, "gugu", "password", "hkkang@woowahan.com");

        final var insertQuery2 = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertQuery2, "neo", "password", "neo@woowahan.com");

        // when, then
        final var sql = "select id, account, password, email from users";
        List<TestUser> users = jdbcTemplate.query(sql, ROW_MAPPER);

        assertAll(() -> assertThat(users).hasSize(2), () -> assertThat(users.get(0).getAccount()).isEqualTo("gugu"),
            () -> assertThat(users.get(1).getAccount()).isEqualTo("neo"),
            () -> assertThat(users.get(0).checkPassword("password")).isTrue(),
            () -> assertThat(users.get(1).checkPassword("password")).isTrue(),
            () -> assertThat(users.get(0).getEmail()).isEqualTo("hkkang@woowahan.com"),
            () -> assertThat(users.get(1).getEmail()).isEqualTo("neo@woowahan.com"));
    }
}
