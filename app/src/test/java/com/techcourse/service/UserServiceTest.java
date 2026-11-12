package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        final var testDataSource = new JdbcDataSource();
        testDataSource.setURL("jdbc:h2:mem:user_service_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        this.dataSource = testDataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        final var actual = userService.findById(1L);
        final var history = jdbcTemplate.query(
                "SELECT password FROM user_history WHERE user_id = ?",
                (rs, rowNum) -> rs.getString("password"),
                1L
        );

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(history).containsExactly(newPassword);
    }

    @Test
    void testTransactionRollback() {
        final var newPassword = "newPassword";
        final var createBy = "gugu";
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory history) {
                final var passwordInTransaction = userDao.findById(1L).orElseThrow().getPassword();
                assertThat(passwordInTransaction).isEqualTo(newPassword);
                super.log(history);
            }
        };
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
    }
}
