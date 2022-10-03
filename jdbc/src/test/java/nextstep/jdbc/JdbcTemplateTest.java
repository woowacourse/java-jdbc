package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

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
        int count = jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)",
                "brorae", "password", "brorae@woowa.com");

        assertThat(count).isEqualTo(1);
    }

    @Test
    void update() {
        jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)",
                "brorae", "password", "brorae@woowa.com");
        int count = jdbcTemplate.update("update users set account=?, password=?, email=? where account=?",
                "rennon", "password123", "rennon@woowa.com", "brorae");

        assertThat(count).isEqualTo(1);
    }

    @Test
    void delete() {
        jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)",
                "brorae", "password", "brorae@woowa.com");
        int count = jdbcTemplate.update("delete from users where id=?", 1L);

        assertThat(count).isEqualTo(1);
    }
}
