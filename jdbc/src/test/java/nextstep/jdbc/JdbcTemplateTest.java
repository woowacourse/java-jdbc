package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nextstep.support.Crew;
import nextstep.support.DataSourceConfig;
import nextstep.support.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<Crew> CREW_ROW_MAPPER = rs -> new Crew(
            rs.getLong("id"),
            rs.getString("nickname"),
            rs.getString("name"),
            rs.getInt("age")
    );
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @Test
    void update() {
        String sql = "INSERT INTO crew (nickname, name, age) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, "pangpang", "seunglae", 15);
        final Crew crew = jdbcTemplate.queryForObject("SELECT * FROM crew WHERE name = 'seunglae'", CREW_ROW_MAPPER);

        assertThat(crew.getNickname()).isEqualTo("pangpang");
    }

    @Test
    void query() {
        jdbcTemplate.update("INSERT INTO crew (nickname, name, age) values ('seungpang', 'seunglae', 20)");
        jdbcTemplate.update("INSERT INTO crew (nickname, name, age) values ('klay', 'dongju', 20)");

        String sql = "SELECT * FROM crew";
        List<Crew> crews = jdbcTemplate.query(sql, CREW_ROW_MAPPER);

        assertThat(crews).hasSize(2);
    }

    @Test
    void queryForObject() {
        jdbcTemplate.update(
                "INSERT INTO crew (nickname, name, age) VALUES (?, ?, ?)", new Object[]{"seungpang", "seunglae", "20"});

        String sql = "SELECT * FROM crew WHERE name = ?";
        final Crew crew = jdbcTemplate.queryForObject(sql, CREW_ROW_MAPPER, "seunglae");

        assertThat(crew.getNickname()).isEqualTo("seungpang");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("truncate table crew");
    }
}
