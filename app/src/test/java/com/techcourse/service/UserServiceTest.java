package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private static final String ACCOUNT_GUGU = "gugu";
    private static final String INITIAL_PASSWORD = "password";
    private static final String NEW_PASSWORD = "newPassword";
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        User user = new User(ACCOUNT_GUGU, INITIAL_PASSWORD, "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        //given
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        UserService userService = new UserService(userDao, userHistoryDao);

        //when
        userService.changePassword(1L, NEW_PASSWORD, ACCOUNT_GUGU);

        User actual = userService.findById(1L).get();

        //then
        assertThat(actual.getPassword()).isEqualTo(NEW_PASSWORD);
    }

    @Test
    void testTransactionRollback() {
        //given
        UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        UserService userService = new UserService(userDao, userHistoryDao);

        long id = userDao.findAll()
                .getLast()
                .getId();

        //when
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(id, NEW_PASSWORD, ACCOUNT_GUGU));

        User actual = userService.findById(id).get();

        //then
        assertAll(
                () -> assertThat(actual.getPassword()).isEqualTo(INITIAL_PASSWORD),
                () -> assertThat(actual.getPassword()).isNotEqualTo(NEW_PASSWORD)
        );
    }
}
