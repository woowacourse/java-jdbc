package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcTemplateTest {

    public static final String QUERY_SELECT_BY_ID = "select * from users where id = ?";

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(DataSourceTestConfig.getInstance());
    }

    @Test
    void insert() {
        jdbcTemplate.update("insert into users (account, password, email) values('pomo2', 'pomo1234', 'pomo2@email.com')");
        Map<String, Object> params = jdbcTemplate.query(QUERY_SELECT_BY_ID, getMapRowMapper(), 2L);
        assertThat(params)
                .containsEntry("id", 2L)
                .containsEntry("account", "pomo2")
                .containsEntry("password", "pomo1234")
                .containsEntry("email", "pomo2@email.com");
    }

    @Test
    void update() {
        jdbcTemplate.update("update users set account='pomo2', password='pomo1234', email='pomo2@email.com' where id = ?", 1L);
        Map<String, Object> params = jdbcTemplate.query(QUERY_SELECT_BY_ID, getMapRowMapper(), 1L);
        assertThat(params)
                .containsEntry("id", 1L)
                .containsEntry("account", "pomo2")
                .containsEntry("password", "pomo1234")
                .containsEntry("email", "pomo2@email.com");
    }

    @Test
    void select() {
        Map<String, Object> params = jdbcTemplate.query("select * from users where id = ?", getMapRowMapper(), 1L);
        assertThat(params)
                .containsEntry("id", 1L)
                .containsEntry("account", "pomo")
                .containsEntry("password", "pomo")
                .containsEntry("email", "pomo@email.com");
    }

    private RowMapper<Map<String, Object>> getMapRowMapper() {
        return rs -> {
            Map<String, Object> result = new HashMap<>();
            if (rs.next()) {
                result.put("id", rs.getLong("id"));
                result.put("account", rs.getString("account"));
                result.put("password", rs.getString("password"));
                result.put("email", rs.getString("email"));
            }
            return result;
        };
    }
}