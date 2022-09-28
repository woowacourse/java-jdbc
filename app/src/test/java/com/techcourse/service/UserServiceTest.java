package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        final var jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        final var userDao = new UserDao(jdbcTemplate);
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);

        this.userService = new UserService(userDao, userHistoryDao, jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userService.insert(user);
    }

    @Test
    void testTransaction() {
        final var newPassword = "qqqqq";
        final var user = userService.findById(1L);
        user.changePassword(newPassword);

        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class, () -> userService.edit(user, "hkkang"));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
