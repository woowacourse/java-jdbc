package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatNoException;

import nextstep.support.DataSourceConfig;
import nextstep.support.DatabasePopulatorUtils;
import nextstep.support.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @Test
    void update() {
        // given
        String sql = "insert into member (name, age) values (?, ?)";

        // when & then
        assertThatNoException().isThrownBy(
                () -> jdbcTemplate.update(sql, new Object[]{"hello jdbc!", 10})
        );
    }

    @Test
    void query() {
        // given
        String sql = "select * from member";

        // when & then
        assertThatNoException().isThrownBy(
                () -> jdbcTemplate.query(sql, ((resultSet, rowNum) -> new Member(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("age")))
                )
        );
    }

    @Test
    void queryForObject() {
        // given
    }
}
