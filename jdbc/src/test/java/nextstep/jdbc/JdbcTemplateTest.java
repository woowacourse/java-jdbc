package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @AfterEach
    void refresh() {
        jdbcTemplate.deleteAll("delete from users");
    }

    @DisplayName("insert 쿼리를 완성시켜 실행시킨다.")
    @Test
    void insert() {
        TestUser user = new TestUser("account", "password", "email");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        Long id = jdbcTemplate.insert(sql, user.getAccount(), user.getPassword(), user.getEmail());

        assertThat(id).isEqualTo(1L);
    }

    @DisplayName("데이터 하나만 반환하는 find 쿼리를 완성시켜 실행시킨다.")
    @Test
    void find() {
        TestUser user = new TestUser("account", "password", "email");
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        Long id = jdbcTemplate.insert(sql, user.getAccount(), user.getPassword(), user.getEmail());

        sql = "select id, account, password, email from users where id = ?";
        Object result = jdbcTemplate.find(TestUser.class, sql, id);

        assertThat((TestUser) result).isEqualTo(user);
    }

    @DisplayName("데이터를 조회하는 finds 쿼리를 완성시켜 실행시킨다.")
    @Test
    void finds() {
        TestUser user = new TestUser("account", "password", "email");
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.insert(sql, user.getAccount(), user.getPassword(), user.getEmail());

        sql = "select id, account, password, email from users where account = ?";
        List<Object> results = jdbcTemplate.finds(TestUser.class, sql, user.getAccount());

        assertAll(
                () -> assertThat(results.size()).isEqualTo(1),
                () -> assertThat(results.get(0)).isEqualTo(user)
        );
    }
}
