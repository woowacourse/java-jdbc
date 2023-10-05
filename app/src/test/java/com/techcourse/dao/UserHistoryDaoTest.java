package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;
    private Connection connection;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        connection = jdbcTemplate.getConnection();

        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    void log가_잘_기록되는지_확인() {
        UserHistory userHistory = new UserHistory(1L, 1L, "jena", "jenapw", "jenaemail", "jena");
        userHistoryDao.log(connection, userHistory);

        List<UserHistory> actual = userHistoryDao.findAll();

        assertThat(actual).usingRecursiveComparison().ignoringFields("createdAt")
                .isEqualTo(List.of(userHistory));
    }
}
