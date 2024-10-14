package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.sql.DataSource;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.JdbcTransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TxUserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        DatabasePopulatorUtils.execute(dataSource);
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @DisplayName("트랜잭션 내에서 실행 중 이상 없이 실행이 완료되면 커밋된다.")
    @Test
    void testTransactionCommit() {
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        JdbcTransactionManager jdbcTransactionManager = new JdbcTransactionManager(dataSource);
        UserService userService = new TxUserService(appUserService, jdbcTransactionManager);

        String newPassword = "newPassword";
        String createBy = "gugu";

        userService.changePassword(1L, newPassword, createBy);
        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @DisplayName("트랜잭션 내에서 실행 중 예외가 발생될 경우 롤백된다.")
    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        JdbcTransactionManager jdbcTransactionManager = new JdbcTransactionManager(dataSource);
        UserService userService = new TxUserService(appUserService, jdbcTransactionManager);

        String newPassword = "newPassword";
        String createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
