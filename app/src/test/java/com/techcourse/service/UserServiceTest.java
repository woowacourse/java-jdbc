package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.service.transaction.TransactionManager;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private Connection connection;
    private TransactionManager transactionManager;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        this.connection = dataSource.getConnection();
        this.transactionManager = new TransactionManager(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (this.connection != null) {
            try {
                this.connection.rollback();
            } finally {
                this.connection.close();
            }
        }
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        final UserService userService = new TxUserService(transactionManager, appUserService);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        // 애플리케이션 서비스
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        // 트랜잭션 서비스 추상화
        final var userService = new TxUserService(transactionManager, appUserService);

        final var newPassword = "newPassword";
        final var createdBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createdBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
