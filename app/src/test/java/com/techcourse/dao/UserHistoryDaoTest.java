package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;
    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DataSource dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("drop table if exists users");
        jdbcTemplate.update("drop table if exists user_history");
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(dataSource);
        userHistoryDao = new UserHistoryDao(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    @DisplayName("삽입 쿼리가 올바르게 수행된다.")
    void insertTest() {
        UserHistory userHistory = new UserHistory(
                1L,
                1L, "pedro", "pedro1234", "pedro@ped.ro", "creator"
        );
        userHistoryDao.log(userHistory);

        int count = jdbcTemplate.queryForObject(
                "select count(*) from user_history",
                rs -> Integer.parseInt(rs.getString(1))
        );
        assertThat(count).isEqualTo(1);
    }
}
