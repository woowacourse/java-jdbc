package com.techcourse.service;

import org.h2.jdbcx.JdbcDataSource;
import java.util.UUID;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(new JdbcTemplate(dataSource));
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(jdbcTemplate.query("select password, created_by, created_at from user_history where user_id = ?",
                rs -> java.util.List.of(rs.getString("password"), rs.getString("created_by"), rs.getTimestamp("created_at")), 1L))
                .singleElement().satisfies(history -> {
                    assertThat(history.get(0)).isEqualTo(newPassword);
                    assertThat(history.get(1)).isEqualTo(createBy);
                    assertThat(history.get(2)).isNotNull();
                });
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(new JdbcTemplate(dataSource));
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history", rs -> rs.getLong(1))).isEmpty();
    }
    @Test
    void rollsBackWhenHistoryInsertFails() {
        final var userService = new TxUserService(new AppUserService(userDao, new UserHistoryDao(new JdbcTemplate(dataSource))), dataSource);

        assertThrows(DataAccessException.class, () -> userService.changePassword(1L, "newPassword", null));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history", rs -> rs.getLong(1))).isEmpty();
    }
}
