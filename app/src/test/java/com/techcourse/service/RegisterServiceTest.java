package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.controller.request.RegisterRequest;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.exception.DuplicateAccountException;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RegisterService는")
class RegisterServiceTest {

    private RegisterService registerService;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());

        registerService = new RegisterService(userDao);
    }

    @DisplayName("register 시도시 이미 존재하는 account가 기입되면 예외가 발생한다.")
    @Test
    void registerException() {
        // given
        String account = "account";
        userDao.insert(new User(account, "pw", "em"));

        RegisterRequest request = new RegisterRequest(account, "password", "email");

        // when, then
        assertThatThrownBy(() -> registerService.registerUser(request))
            .isExactlyInstanceOf(DuplicateAccountException.class);
    }
}