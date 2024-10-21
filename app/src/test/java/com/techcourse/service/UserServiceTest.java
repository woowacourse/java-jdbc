package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        DataSource dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        transactionManager = new TransactionManager(dataSource);
        DatabasePopulatorUtils.execute(dataSource);
        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1;");
    }

    @DisplayName("유저 정보를 조회한다.")
    @Test
    void findById() {
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        AppUserService userService = new AppUserService(userDao, userHistoryDao);
        String account = "daon";
        userDao.insert(new User(account, "1234", "test@test.com"));

        User result = userService.findById(2L);

        assertThat(result.account()).isEqualTo(account);
    }

    @DisplayName("비밀번호가 올바르게 변경된다.")
    @Test
    void testChangePassword() {
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        AppUserService userService = new AppUserService(userDao, userHistoryDao);
        String newPassword = "qqqqq";
        String createdBy = "gugu";
        userService.changePassword(1L, newPassword, createdBy);

        User actual = userService.findById(1L);

        assertThat(actual.password()).isEqualTo(newPassword);
    }

    @DisplayName("트랜잭션 도중 예외가 발생하면 데이터 변경이 롤백된다.")
    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        MockUserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        // 애플리케이션 서비스
        UserService appUserService = new AppUserService(userDao, userHistoryDao);
        // 트랜잭션 서비스 추상화
        UserService userService = new TxUserService(appUserService, transactionManager);

        String newPassword = "newPassword";
        String createdBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThatThrownBy(() -> userService.changePassword(1L, newPassword, createdBy))
                .isInstanceOf(DataAccessException.class);

        User actual = userService.findById(1L);

        assertThat(actual.password()).isNotEqualTo(newPassword);
    }
}
