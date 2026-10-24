package com.techcourse.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private static final String HUNDRED = "a".repeat(100);

    @Test
    void acceptsValuesUpToHundredCharacters() {
        final var user = new User(HUNDRED, HUNDRED, HUNDRED);

        assertThat(user.getAccount()).hasSize(100);
    }

    @Test
    void rejectsMissingPassword() {
        assertThatThrownBy(() -> new User("gugu", null, "hkkang@woowahan.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password");
    }

    @Test
    void rejectsBlankAccount() {
        assertThatThrownBy(() -> new User("   ", "password", "hkkang@woowahan.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("account");
    }

    @Test
    void rejectsEmailLongerThanHundredCharacters() {
        assertThatThrownBy(() -> new User("gugu", "password", HUNDRED + "b"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    void rejectsInvalidNewPassword() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");

        assertThatThrownBy(() -> user.changePassword(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(user.checkPassword("password")).isTrue();
    }
}
