package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TxUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private TransactionTemplate transactionTemplate;
    private User user;

    @BeforeEach
    void setUp() {
        DataSource dataSource = DataSourceConfig.getInstance();

        DatabasePopulatorUtils.execute(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);
        this.transactionTemplate = new TransactionTemplate(dataSource);

        userDao.deleteAll();


        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        this.user = userDao.findByAccount("gugu");
    }

    @Test
    void testTransactionRollback() {
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService, transactionTemplate);

        final var newPassword = "newPassword";
        final var createBy = "gugu";

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(user.getId(), newPassword, createBy));

        final var actual = userService.findById(user.getId());

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
