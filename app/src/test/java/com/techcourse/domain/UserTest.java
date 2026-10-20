package com.techcourse.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void rejectsNullFields() {
        assertThatThrownBy(() -> new User(null, "password", "user@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new User("account", null, "user@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new User("account", "password", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsFieldsLongerThanColumnLength() {
        final var tooLong = "a".repeat(101);

        assertThatThrownBy(() -> new User(tooLong, "password", "user@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new User("account", tooLong, "user@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new User("account", "password", tooLong))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsFieldsAtColumnLength() {
        final var value = "a".repeat(100);

        final var user = new User(value, value, value);

        assertThat(user.getAccount()).isEqualTo(value);
        assertThat(user.getPassword()).isEqualTo(value);
        assertThat(user.getEmail()).isEqualTo(value);
    }

    @Test
    void validatesFieldsWhenIdIsProvided() {
        assertThatThrownBy(() -> new User(1L, null, "password", "user@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new User(1L, "account", "a".repeat(101), "user@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidPasswordChangePreservesPassword() {
        final var user = new User("account", "password", "user@example.com");

        assertThatThrownBy(() -> user.changePassword(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> user.changePassword("a".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void acceptsPasswordChangeAtColumnLength() {
        final var user = new User("account", "password", "user@example.com");
        final var password = "a".repeat(100);

        user.changePassword(password);

        assertThat(user.getPassword()).isEqualTo(password);
    }
}
