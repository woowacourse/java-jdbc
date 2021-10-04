package com.techcourse.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User는")
class UserTest {

    @DisplayName("password 검증 실패시 예외가 발생한다.")
    @Test
    void checkPasswordException() {
        // given
        User user = new User("account", "password", "email");

        // when, then
        assertThatThrownBy(() -> user.checkPassword("WRONG_PASSWORD")).isExactlyInstanceOf(
            UnauthorizedException.class);
    }
}