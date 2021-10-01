package com.techcourse.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserTest")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("gugu", "password", "gugu@gmail.com");
    }

    @Test
    @DisplayName("비밀번호가 맞다면 true 를 반환한다..")
    void checkPasswordWhenTrue() {
        assertThat(user.checkPassword("password")).isTrue();
    }

    @Test
    @DisplayName("비밀번호가 틀리면 false 를 반환한다..")
    void checkPasswordWhenFalse() {
        assertThat(user.checkPassword("pass")).isFalse();
    }

    @Test
    @DisplayName("패스워드를 변경한다.")
    void changePassword() {
        // given
        String password = "change";

        // when
        user.changePassword(password);

        // then
        assertThat(user.checkPassword(password)).isTrue();
    }
}