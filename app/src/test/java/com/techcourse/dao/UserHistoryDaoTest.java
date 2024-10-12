package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementResolver;
import com.interface21.jdbc.datasource.DataAccessWrapper;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.rowmapper.UserHistoryRowMapper;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, dataSource.getConnection());

        this.jdbcTemplate = new JdbcTemplate(new DataAccessWrapper(DataSourceConfig.getInstance()), new PreparedStatementResolver());
        this.userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.queryForUpdate("DELETE FROM user_history");
        jdbcTemplate.queryForUpdate("ALTER TABLE user_history ALTER COLUMN id RESTART WITH 1");
    }

    @DisplayName("새로운 유저 기록을 DB에 저장할 수 있다")
    @Test
    void log() {
        User user = new User(1L, "loki", "password", "hkkang@woowahan.com");
        UserHistory history = new UserHistory(user, "coli");

        userHistoryDao.log(history);

        UserHistory savedHistory = (UserHistory) jdbcTemplate.queryForObject(
                "select * from user_history where user_id = ?",
                new UserHistoryRowMapper(),
                1L
        );

        assertThat(history)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt")
                .isEqualTo(savedHistory);
    }
}
