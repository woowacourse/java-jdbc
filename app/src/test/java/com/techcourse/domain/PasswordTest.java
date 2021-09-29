package com.techcourse.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Password는")
class PasswordTest {

    private Password password;

    @DisplayName("제공 받은 값이 자신과 다른지 확인할 때")
    @Nested
    class IsDifferentWith {

        private static final String VALUE = "PASSWORD";

        @BeforeEach
        void setUp() {
            password = new Password(VALUE);
        }

        @DisplayName("다르다면 True를 반환한다.")
        @Test
        void isDifferentTrue() {
            // when, then
            assertThat(password.isDifferentWith("ANOTHER" + VALUE)).isTrue();
        }

        @DisplayName("같다면 False를 반환한다.")
        @Test
        void isDifferentFalse() {
            // when, then
            assertThat(password.isDifferentWith(VALUE)).isFalse();
        }
    }
}