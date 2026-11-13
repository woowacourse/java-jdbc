package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private DataSource dataSource;
    private Connection keepAliveConnection;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        final var testDataSource = new JdbcDataSource();
        testDataSource.setURL("jdbc:h2:mem:user-service-" + UUID.randomUUID());
        this.dataSource = testDataSource;
        this.keepAliveConnection = dataSource.getConnection();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final UserService userService = new TxUserService(appUserService, dataSource);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        final var actual = userService.findById(1L);
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ? and password = ?",
                rs -> rs.getInt(1), 1L, newPassword);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final UserService userService = new TxUserService(appUserService, dataSource);

        final var originalPassword = userService.findById(1L).getPassword();
        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(originalPassword);
    }
}
