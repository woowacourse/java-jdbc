package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatNoException;

import nextstep.support.DataSourceConfig;
import nextstep.support.DatabasePopulatorUtils;
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

        // when
        assertThatNoException().isThrownBy(
                () -> jdbcTemplate.update(sql, new Object[]{"hello jdbc!", 10})
        );
    }
}
