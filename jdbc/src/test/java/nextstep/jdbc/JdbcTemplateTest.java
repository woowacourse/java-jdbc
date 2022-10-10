package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nextstep.support.DataSourceConfig;
import nextstep.support.DatabasePopulatorUtils;
import nextstep.support.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getInt("age"));
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance(), "schema.sql");
    }

    @Test
    void update() {
        // given
        String sql = "insert into member (name, age) values (?, ?)";

        // when & then
        jdbcTemplate.update(sql, "hello jdbc!", 10);

        String assertSql = "select * from member where age = 10";
        Member member = jdbcTemplate.queryForObject(assertSql, MEMBER_ROW_MAPPER);
        assertThat(member.getName()).isEqualTo("hello jdbc!");
    }

    @Test
    void query() {
        // given
        jdbcTemplate.update("insert into member (name, age) values ('hello', 10)");
        String sql = "select * from member";

        // when
        List<Member> members = jdbcTemplate.query(sql, MEMBER_ROW_MAPPER);

        // then
        assertThat(members).hasSize(1);
    }

    @Test
    void queryForObject() {
        // given
        jdbcTemplate.update("insert into member (name, age) values ('hello', 10)");

        // when
        String sql = "select * from member where age = ?";
        Member member = jdbcTemplate.queryForObject(sql, MEMBER_ROW_MAPPER, 10);

        // then
        assertThat(member.getName()).isEqualTo("hello");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("drop table member if exists");
    }
}
