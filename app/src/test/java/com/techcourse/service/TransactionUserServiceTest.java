package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionUserServiceTest {

    private static final String ACCOUNT_GUGU = "gugu";
    private static final String INITIAL_PASSWORD = "password";
    private static final String NEW_PASSWORD = "newPassword";

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private MockUserHistoryDao mockUserHistoryDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        mockUserHistoryDao = new MockUserHistoryDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        User user = new User(ACCOUNT_GUGU, INITIAL_PASSWORD, "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testTransactionRollback_save() {
        //given
        userDao = new MockUserDao(jdbcTemplate);
        AppUserService appUserService = new AppUserService(userDao, mockUserHistoryDao);
        TransactionUserService userService = new TransactionUserService(appUserService);

        //when
        assertThrows(DataAccessException.class,
                () -> userService.save(new User("Ash", "password", "ash@techcourse.com")));

        long id = userDao.findAll()
                .getLast()
                .getId();

        User actual = userService.findById(id).get();

        //then
        assertAll(
                () -> assertThat(actual.getAccount()).isEqualTo(ACCOUNT_GUGU),
                () -> assertThat(actual.getAccount()).isNotEqualTo("Ash")
        );
    }

    @Test
    void testTransactionRollback_changePassword() {
        //given
        AppUserService appUserService = new AppUserService(userDao, mockUserHistoryDao);
        TransactionUserService userService = new TransactionUserService(appUserService);

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
