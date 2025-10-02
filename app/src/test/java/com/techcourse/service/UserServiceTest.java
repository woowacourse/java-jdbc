package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.simple.SimpleUserDao;
import com.techcourse.dao.simple.SimpleUserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled
class UserServiceTest {

    private SimpleUserDao userDao;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.userDao = new SimpleUserDao(dataSource);
        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new SimpleUserHistoryDao(dataSource);
        final var userService = new UserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        final var user = userDao.findByAccount(createBy);
        userService.changePassword(user.getId(), newPassword, createBy);

        final var actual = userDao.findById(user.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new SimpleUserHistoryDao(dataSource) {
            @Override
            public void log(final UserHistory userHistory) {
                throw new DataAccessException("롤백 테스트를 위한 예외");
            }
        };
        final var userService = new UserService(userDao, userHistoryDao);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        final var user = userDao.findByAccount(createBy);
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(user.getId(), newPassword, createBy));

        final var actual = userDao.findById(user.getId());

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
