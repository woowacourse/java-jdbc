package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    void log() {
        //given
        final String account = "gugu";
        userDao.insert(new User(account, "password", "gugu@woowahan.com"));
        final User gugu = userDao.findByAccount(account);
        final UserHistory expected = new UserHistory(gugu, "rosie");

        //when
        userHistoryDao.log(expected);

        //then
        final UserHistory actual = jdbcTemplate.queryForObject("select * from user_history where id = ?",
                (resultSet, rowNum) ->
                        new UserHistory(
                                resultSet.getLong("id"),
                                resultSet.getLong("user_id"),
                                resultSet.getString("account"),
                                resultSet.getString("password"),
                                resultSet.getString("email"),
                                resultSet.getString("created_by")
                        ), gugu.getId());

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt")
                .isEqualTo(expected);
    }
}
