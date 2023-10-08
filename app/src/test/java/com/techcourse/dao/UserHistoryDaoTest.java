package com.techcourse.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    void log() throws SQLException {
        Connection connection = DataSourceConfig.getInstance().getConnection();
        User user = new User(1, "gray", "password", "gray@gmail.com");
        UserHistory userHistory = new UserHistory(user, LocalDate.now().toString());

        assertDoesNotThrow(() -> userHistoryDao.log(connection, userHistory));
    }
}
