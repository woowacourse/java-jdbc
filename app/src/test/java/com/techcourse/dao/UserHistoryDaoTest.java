package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    @BeforeEach
    void setUp() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        new JdbcTemplate(dataSource).update("truncate table user_history restart identity");
    }

    @Test
    @DisplayName("DataSource를 주입받아 사용자 변경 이력을 저장한다")
    void save_history_with_data_source() throws SQLException {
        assertPersistsHistory(new UserHistoryDao(DataSourceConfig.getInstance()));
    }

    @Test
    @DisplayName("JdbcTemplate을 주입받아 사용자 변경 이력을 저장한다")
    void save_history_with_jdbc_template() throws SQLException {
        assertPersistsHistory(new UserHistoryDao(new JdbcTemplate(DataSourceConfig.getInstance())));
    }

    private void assertPersistsHistory(final UserHistoryDao userHistoryDao) throws SQLException {
        final var user = new User(7L, "gugu", "password", "hkkang@woowahan.com");
        final var history = new UserHistory(user, "admin");

        userHistoryDao.log(history);

        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.prepareStatement("select * from user_history");
             final var resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("id")).isEqualTo(1L);
            assertThat(resultSet.getLong("user_id")).isEqualTo(7L);
            assertThat(resultSet.getString("account")).isEqualTo("gugu");
            assertThat(resultSet.getString("password")).isEqualTo("password");
            assertThat(resultSet.getString("email")).isEqualTo("hkkang@woowahan.com");
            assertThat(resultSet.getString("created_by")).isEqualTo("admin");
            assertThat(resultSet.getObject("created_at", LocalDateTime.class))
                    .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.MICROS));
            assertThat(resultSet.next()).isFalse();
        }
    }
}
