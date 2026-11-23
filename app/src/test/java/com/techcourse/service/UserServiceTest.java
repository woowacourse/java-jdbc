package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.transactionTemplate = new TransactionTemplate(DataSourceConfig.getInstance());
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
        final var userService = new TxUserService(transactionTemplate, new AppUserService(userDao, userHistoryDao));

        final var newPassword = "qqqqq";
        final var createdBy = "gugu";
        userService.changePassword(1L, newPassword, createdBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        final var histories = jdbcTemplate.query("select * from user_history where user_id = ?", (rs, rowNum) -> {
            assertThat(rs.getString("account")).isEqualTo("gugu");
            assertThat(rs.getString("password")).isEqualTo(newPassword);
            assertThat(rs.getString("email")).isEqualTo("hkkang@woowahan.com");
            assertThat(rs.getString("created_by")).isEqualTo(createdBy);
            assertThat(rs.getTimestamp("created_at")).isNotNull();
            return rs.getLong("id");
        }, 1L);
        assertThat(histories).hasSize(1);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        // 애플리케이션 서비스
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        // 트랜잭션 서비스 추상화
        final var userService = new TxUserService(transactionTemplate, appUserService);

        final var newPassword = "newPassword";
        final var createdBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createdBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }

    @Test
    void rollsBackPasswordAndAlreadyInsertedHistory() {
        final var failure = new DataAccessException("failure after history insert");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory history) {
                super.log(history);
                throw failure;
            }
        };
        final var userService = new TxUserService(transactionTemplate, new AppUserService(userDao, userHistoryDao));

        assertThat(assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"))).isSameAs(failure);

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history where user_id = ?",
                (rs, rowNum) -> rs.getLong("id"), 1L)).isEmpty();
    }

    @Test
    void rejectsNestedChangePasswordBeforeChangingAnything() {
        final var userService = new TxUserService(transactionTemplate,
                new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));

        // The outer transaction swallows the inner failure and commits.
        transactionTemplate.executeWithoutResult(() ->
                assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", null))
                        .isInstanceOf(IllegalStateException.class));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history where user_id = ?",
                (rs, rowNum) -> rs.getLong("id"), 1L)).isEmpty();
    }
    @Test
    void appUserServiceAloneDoesNotRollBackEarlierChanges() {
        final var userService = new AppUserService(userDao, new MockUserHistoryDao(jdbcTemplate));

        assertThrows(DataAccessException.class, () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("newPassword");
    }

    @Test
    void daoCallsAfterTransactionUseFreshConnections() {
        final var userService = new TxUserService(transactionTemplate,
                new AppUserService(userDao, new UserHistoryDao(jdbcTemplate)));
        userService.changePassword(1L, "first", "gugu");

        userService.save(new User("timo", "password", "timo@woowahan.com"));
        userService.changePassword(2L, "second", "timo");

        assertThat(userService.findById(1L).getPassword()).isEqualTo("first");
        assertThat(userService.findById(2L).getPassword()).isEqualTo("second");
    }
}