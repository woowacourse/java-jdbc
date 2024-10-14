package com.techcourse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.TransactionTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TxUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);
        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        jdbcTemplate.update("DROP TABLE IF EXISTS user_history");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @DisplayName("예외가 발생하면 트랜잭션을 롤백한다.")
    @Test
    void testTransactionRollback() {
        // given
        UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        UserService appUserService = new AppUserService(userDao, userHistoryDao);
        UserService txUserService = new TxUserService(appUserService, transactionTemplate);

        // when
        String newPassword = "newPassword";
        String createdBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> txUserService.changePassword(1L, newPassword, createdBy));

        // then
        User actual = txUserService.getById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
