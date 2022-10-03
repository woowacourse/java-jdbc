package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nextstep.support.Crew;
import nextstep.support.DataSourceConfig;
import nextstep.support.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("update 메서드가 정상적으로 실행되었는지 확인한다.")
    void update() {
        String sql = "INSERT INTO crew (nickname, name, age) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, "pangpang", "seunglae", 15);
        final Crew crew = jdbcTemplate.queryForObject("SELECT * FROM crew WHERE name = 'seunglae'", CREW_ROW_MAPPER);

        assertThat(crew.getNickname()).isEqualTo("pangpang");
    }

    @Test
    @DisplayName("query 메서드가 정상적으로 실행되었는지 확인한다.")
    void query() {
        jdbcTemplate.update("INSERT INTO crew (nickname, name, age) values ('seungpang', 'seunglae', 20)");
        jdbcTemplate.update("INSERT INTO crew (nickname, name, age) values ('klay', 'dongju', 20)");

        String sql = "SELECT * FROM crew";
        List<Crew> crews = jdbcTemplate.query(sql, CREW_ROW_MAPPER);

        assertThat(crews).hasSize(2);
    }

    @Test
    @DisplayName("queryForObject 메서드가 정상적으로 실행되엇는지 확인한다.")
    void queryForObject() {
        jdbcTemplate.update(
                "INSERT INTO crew (nickname, name, age) VALUES (?, ?, ?)", new Object[]{"seungpang", "seunglae", "20"});

        String sql = "SELECT * FROM crew WHERE name = ?";
        final Crew crew = jdbcTemplate.queryForObject(sql, CREW_ROW_MAPPER, "seunglae");

        assertThat(crew.getNickname()).isEqualTo("seungpang");
    }

    @Test
    @DisplayName("queryForObject의 조회결과가 없으면 예외 발생")
    void throwExceptionQueryForObjectIsEmpty() {
        String sql = "SELECT * FROM crew WHERE name = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, CREW_ROW_MAPPER, "seunglae"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("queryForObject의 조회 결과가 1보다 크면 예외 발생")
    void throwExceptionQueryForObjectIsSizeOver() {
        jdbcTemplate.update("INSERT INTO crew (nickname, name, age) VALUES (?, ?, ?)",
                new Object[]{"seungpang", "seunglae", "20"});
        jdbcTemplate.update("INSERT INTO crew (nickname, name, age) VALUES (?, ?, ?)",
                new Object[]{"seungpang", "seunglae", "20"});


        String sql = "SELECT * FROM crew WHERE name = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, CREW_ROW_MAPPER, "seunglae"))
                .isInstanceOf(DataAccessException.class);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("truncate table crew");
    }
}
