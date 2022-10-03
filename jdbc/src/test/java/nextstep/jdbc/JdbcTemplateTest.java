package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<UserObject> OBJECT_ROW_MAPPER =
            (rs, rowNum) -> new UserObject
                    (
                            rs.getLong("id"),
                            rs.getString("account"),
                            rs.getString("password"),
                            rs.getString("email")
                    );

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        DatabasePopulatorUtils.execute(jdbcDataSource);
        jdbcTemplate = new JdbcTemplate(jdbcDataSource);
    }

    @Test
    void insert() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        int count = jdbcTemplate.update(insertSql, "brorae", "password", "brorae@woowa.com");

        String selectSql = "select id, account, password, email from users";
        List<UserObject> users = jdbcTemplate.query(selectSql, OBJECT_ROW_MAPPER);

        assertAll(
                () -> assertThat(count).isEqualTo(1),
                () -> assertThat(users.get(0)).isEqualTo(new UserObject(1L, "brorae", "password", "brorae@woowa.com"))
        );
    }

    @Test
    void update() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, "brorae", "password", "brorae@woowa.com");

        String updateSql = "update users set account=?, password=?, email=? where id=?";
        int count = jdbcTemplate.update(updateSql, "rennon", "password123", "rennon@woowa.com", 1L);

        String selectSql = "select id, account, password, email from users";
        List<UserObject> users = jdbcTemplate.query(selectSql, OBJECT_ROW_MAPPER);

        assertAll(
                () -> assertThat(count).isEqualTo(1),
                () -> assertThat(users.get(0)).isEqualTo(new UserObject(1L, "rennon", "password123", "rennon@woowa.com"))
        );
    }

    @Test
    void queryForObject() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, "brorae", "password", "brorae@woowa.com");

        String selectSql = "select id, account, password, email from users where id=?";
        UserObject userObject = jdbcTemplate.queryForObject(selectSql, OBJECT_ROW_MAPPER, 1L);

        assertThat(userObject).isEqualTo(new UserObject(1L, "brorae", "password", "brorae@woowa.com"));
    }

    @Test
    void query() {
        String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, "brorae", "password", "brorae@woowa.com");

        List<UserObject> users = jdbcTemplate.query("select id, account, password, email from users", OBJECT_ROW_MAPPER);

        assertThat(users).hasSize(1);
    }
}
