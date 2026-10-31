package com.techcourse.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("저장된 비밀번호와 같으면 일치한다고 판단한다")
    void passwordMatches() {
        // given
        final var user = new User("gugu", "password", "gugu@example.com");

        // when
        final var matches = user.checkPassword("password");

        // then
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("저장된 비밀번호와 다르면 일치하지 않는다고 판단한다")
    void passwordDoesNotMatch() {
        // given
        final var user = new User("gugu", "password", "gugu@example.com");

        // when
        final var matches = user.checkPassword("wrongPassword");

        // then
        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("비밀번호를 변경하면 새 비밀번호로만 일치한다고 판단한다")
    void changePassword() {
        // given
        final var user = new User("gugu", "oldPassword", "gugu@example.com");

        // when
        user.changePassword("newPassword");

        // then
        assertThat(user.checkPassword("newPassword")).isTrue();
        assertThat(user.checkPassword("oldPassword")).isFalse();
    }

    @Test
    @DisplayName("비밀번호를 변경해도 사용자 식별 정보는 유지한다")
    void preserveIdentityWhenPasswordChanges() {
        // given
        final var user = new User(1L, "gugu", "oldPassword", "gugu@example.com");

        // when
        user.changePassword("newPassword");

        // then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("gugu");
        assertThat(user.getEmail()).isEqualTo("gugu@example.com");
    }
}
