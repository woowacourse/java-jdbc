package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionManager;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new AppUserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testChangePasswordWithUserHistory() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new AppUserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);
        final List<UserHistory> logs = userService.findLogsByUserId(1L);

        final var actual = userService.findById(1L);

        SoftAssertions.assertSoftly(
                soft -> {
                    soft.assertThat(actual.getPassword()).isEqualTo(newPassword);
                    soft.assertThat(logs).hasSize(1);
                }
        );
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        // 애플리케이션 서비스
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        // 트랜잭션 서비스 추상화
        final var userService = new TxUserService(appUserService, new TransactionManager(dataSource));

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
