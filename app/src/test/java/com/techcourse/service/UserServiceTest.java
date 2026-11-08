package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var newPassword = "qqqqq";
        final var createdBy = "gugu";
        userService.changePassword(1L, newPassword, createdBy);

        final var actual = userService.findById(1L);
        final var histories = userHistoryDao.findByUserId(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(histories).singleElement()
                .satisfies(history -> {
                    assertThat(history.getPassword()).isEqualTo(newPassword);
                    assertThat(history.getCreateBy()).isEqualTo(createdBy);
                });
    }

    @Test
    void rollsBackPasswordAndHistoryWhenRuntimeExceptionOccurs() {
        final var original = new IllegalStateException("이력 저장 후 실패");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                super.log(userHistory);
                throw original;
            }
        };
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao));

        final var actual = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actual.getCause()).isSameAs(original);
        assertThat(TransactionSynchronizationManager.getResource(DataSourceConfig.getInstance())).isNull();
        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertThat(userHistoryDao.findByUserId(1L)).isEmpty();
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        // 애플리케이션 서비스
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        // 트랜잭션 서비스 추상화
        final var userService = new TxUserService(appUserService);

        final var newPassword = "newPassword";
        final var createdBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createdBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
