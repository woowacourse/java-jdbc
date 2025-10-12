package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.TransactionManager;
import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private UserDao userDao;
    private DataSource dataSource;
    private TransactionTemplate transactionTemplate;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.userDao = new UserDao(dataSource);
        this.transactionManager = new TransactionManager(dataSource);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        DatabasePopulatorUtils.execute(dataSource);
        userDao.deleteAll();
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(dataSource);
        final var userService = new AppUserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        final var user = userDao.findByAccount(createBy);
        userService.changePassword(user.getId(), newPassword, createBy);

        final var actual = userDao.findById(user.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        final var userHistoryDao = new MockUserHistoryDao(dataSource);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService, transactionTemplate);

        final var newPassword = "newPassword";
        final var createdBy = "gugu";

        final var user = userDao.findByAccount(createdBy);
        final var originalPassword = user.getPassword();

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(user.getId(), newPassword, createdBy));

        final var actual = userService.findById(user.getId());
        assertThat(actual.getPassword()).isEqualTo(originalPassword);
    }
}
