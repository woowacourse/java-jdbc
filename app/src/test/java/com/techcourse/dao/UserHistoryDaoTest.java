package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    @Test
    @DisplayName("가변 인자로 전달한 사용자 이력의 숫자·문자열·시간 값을 실제 DB에 저장한다")
    void savesEveryHistoryFieldUsingVarargsBinding() throws Exception {
        final var dataSource = DataSourceConfig.createDataSource("jdbc:h2:mem:history-" + UUID.randomUUID());
        try (final var databaseAnchor = dataSource.getConnection()) {
            DatabasePopulatorUtils.execute(dataSource);
            final var jdbcTemplate = new JdbcTemplate(dataSource);
            final var history = new UserHistory(new User(7L, "gugu", "newPassword", "gugu@example.com"), "operator");

            new UserHistoryDao(jdbcTemplate).log(history);

            final var saved = jdbcTemplate.queryForObject("select * from user_history", row ->
                    new SavedHistory(row.getLong("user_id"), row.getString("account"), row.getString("password"),
                            row.getString("email"), row.getTimestamp("created_at").toLocalDateTime(),
                            row.getString("created_by")));
            assertThat(saved.userId()).isEqualTo(history.getUserId());
            assertThat(saved.account()).isEqualTo(history.getAccount());
            assertThat(saved.password()).isEqualTo(history.getPassword());
            assertThat(saved.email()).isEqualTo(history.getEmail());
            assertThat(saved.createdAt()).isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(saved.createdBy()).isEqualTo(history.getCreateBy());
        }
    }

    private record SavedHistory(long userId, String account, String password, String email,
                                LocalDateTime createdAt, String createdBy) {}
}
