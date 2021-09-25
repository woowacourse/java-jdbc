package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.exception.UserNotFoundException;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserService는")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        UserDao userDao = new UserDao(DataSourceConfig.getInstance());
        userService = new UserService(userDao);
    }

    @DisplayName("account로 유저 탐색시 일치하는 유저가 없으면 예외가 발생한다.")
    @Test
    void findUserByAccountException() {
        // when, then
        assertThatThrownBy(() -> userService.findUserByAccount("라이언"))
            .isExactlyInstanceOf(UserNotFoundException.class);
    }
}