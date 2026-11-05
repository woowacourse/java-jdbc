package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    @Test
    void 사용자_이력을_저장하면_사용자_정보와_변경_시각과_변경자를_다시_조회할_수_있다() {
        // given
        JdbcTemplate template = new JdbcTemplate(DataSourceConfig.getInstance());
        template.update("drop table if exists user_history");
        template.update("drop table if exists users");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        UserHistory history = new UserHistory(new User(42L, "account", "password", "email"), "admin");

        // when
        new UserHistoryDao(template).log(history);

        // then
        assertThat(template.query("select * from user_history", row -> {
            assertThat(row.getLong("user_id")).isEqualTo(42L);
            assertThat(row.getString("account")).isEqualTo("account");
            assertThat(row.getString("password")).isEqualTo("password");
            assertThat(row.getString("email")).isEqualTo("email");
            assertThat(row.getTimestamp("created_at").toLocalDateTime())
                .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(row.getString("created_by")).isEqualTo("admin");
            return row.getLong("id");
        })).hasSize(1);
    }
}
