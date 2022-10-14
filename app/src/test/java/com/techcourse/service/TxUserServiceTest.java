package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

class TxUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private DataSourceTransactionManager transactionManager;
    private UserDao userDao;
    private UserHistoryDao userHistoryDao;

    private static final String USER_ACCOUNT = "gugu";
    private static final User USER = new User(USER_ACCOUNT, "password", "hkkang@woowahan.com");

    private long savedUserId;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        transactionManager = new DataSourceTransactionManager(jdbcTemplate.getDataSource());
        userDao = new UserDao(jdbcTemplate);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);

        DatabasePopulatorUtils.init(DataSourceConfig.getInstance());

        userDao.insert(USER);
        savedUserId = userDao.findByAccount(USER.getAccount()).getId();
    }

    @AfterEach
    void tearDown() {
        DatabasePopulatorUtils.clear(DataSourceConfig.getInstance());
    }

    @DisplayName("사용자 비밀번호 변경")
    @Test
    void changePassword() {
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);

        final TxUserService userService = new TxUserService(transactionManager, appUserService);
        userService.changePassword(savedUserId, "newPassword", USER_ACCOUNT);

        final User user = userService.findById(savedUserId);
        assertThat(user.getPassword()).isEqualTo("newPassword");
    }

    @DisplayName("사용자 비밀번호 변경 실패 시 롤백")
    @Test
    void changePasswordRollback() {
        final MockUserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);

        final TxUserService userService = new TxUserService(transactionManager, appUserService);

        assertThrows(DataAccessException.class,
            () -> userService.changePassword(savedUserId, "newPassword", USER_ACCOUNT));

        final User actual = userService.findById(savedUserId);
        assertThat(actual.getPassword()).isNotEqualTo("newPassword");
    }
}
