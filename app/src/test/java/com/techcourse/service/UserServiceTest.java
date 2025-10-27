package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private long userId;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        // 테이블 초기화
        try {
            jdbcTemplate.update("DELETE FROM user_history");
            jdbcTemplate.update("DELETE FROM users");
        } catch (Exception e) {
            // 테이블이 없으면 무시
        }

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        // DB에 삽입된 유저의 실제 ID를 가져옵니다
        this.userId = userDao.findByAccount("gugu").orElseThrow().getId();
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, DataSourceConfig.getInstance());

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(userId, newPassword, createBy);

        final var actual = userService.findById(userId);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, DataSourceConfig.getInstance());

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(userId, newPassword, createBy));

        final var actual = userService.findById(userId);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
