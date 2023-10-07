package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserHistoryDaoTest {

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @Test
    @DisplayName("userHistory를 저장한다.")
    void log() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        final UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserHistory userHistory = new UserHistory(1L, 2L, "hong-sile", "password", "email",
            "2023");

        userHistoryDao.log(userHistory);

        final List<UserHistory> userHistories = jdbcTemplate.executeQuery(
            "select * from user_history",
            UserHistoryDaoTest::userHistoryMapper
        );
        assertThat(userHistories)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("createdAt")
            .containsExactly(userHistory);
    }

    private static List<UserHistory> userHistoryMapper(final ResultSet resultSet)
        throws SQLException {
        final List<UserHistory> userHistories = new ArrayList<>();
        do {
            userHistories.add(new UserHistory(
                resultSet.getLong(1),
                resultSet.getLong(2),
                resultSet.getString(3),
                resultSet.getString(4),
                resultSet.getString(5),
                resultSet.getString(7)
            ));
        } while (resultSet.next());
        return userHistories;
    }
}
