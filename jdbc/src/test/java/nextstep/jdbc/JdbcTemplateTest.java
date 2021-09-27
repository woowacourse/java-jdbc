package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcTemplateTest {

    public static final String QUERY_SELECT_BY_ID = "select * from users where id = ?";

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DatabasePopulatorTestUtils.execute(DataSourceTestConfig.getInstance());
        this.jdbcTemplate = new JdbcTemplate(DataSourceTestConfig.getInstance());
    }

    @Test
    void insert() {
        jdbcTemplate.update("insert into users values(2, 'pomo2', 'pomo1234', 'pomo2@email.com')");
        Optional<Map<String, Object>> params = jdbcTemplate.queryForObject(QUERY_SELECT_BY_ID, getMapRowMapper(), 2L);
        assertThat(params).isPresent();
        유저_동등성_비교(params.get(), 2L, "pomo2", "pomo1234", "pomo2@email.com");
    }

    @Test
    void update() {
        jdbcTemplate.update("update users set account='pomo2', password='pomo1234', email='pomo2@email.com' where id = ?", 1L);
        Optional<Map<String, Object>> params = jdbcTemplate.queryForObject(QUERY_SELECT_BY_ID, getMapRowMapper(), 1L);
        assertThat(params).isPresent();
        유저_동등성_비교(params.get(), 1L, "pomo2", "pomo1234", "pomo2@email.com");
    }

    @Test
    void select() {
        Optional<Map<String, Object>> params = jdbcTemplate.queryForObject("select * from users where id = ?", getMapRowMapper(), 1L);
        assertThat(params).isPresent();
        유저_동등성_비교(params.get(), 1L, "pomo", "pomo", "pomo@email.com");
    }

    @Test
    void findAll() {
        jdbcTemplate.update("insert into users values(2, 'pomo2', 'pomo2', 'pomo2@email.com')");
        List<Map<String, Object>> params = jdbcTemplate.query("select * from users", getMapRowMapper());
        assertThat(params.size()).isNotZero();
        유저_동등성_비교(params.get(0), 1L, "pomo", "pomo", "pomo@email.com");
    }

    private void 유저_동등성_비교(Map<String, Object> params, long id, String account, String password, String email) {
        assertThat(params)
                .containsEntry("id", id)
                .containsEntry("account", account)
                .containsEntry("password", password)
                .containsEntry("email", email);
    }

    private RowMapper<Map<String, Object>> getMapRowMapper() {
        return rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", rs.getLong("id"));
            result.put("account", rs.getString("account"));
            result.put("password", rs.getString("password"));
            result.put("email", rs.getString("email"));
            return result;
        };
    }
}