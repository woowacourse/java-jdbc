package com.techcourse.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserSessionTest")
class UserSessionTest {

    private HttpSession httpSession;

    @BeforeEach
    void setup() {
        httpSession = mock(HttpSession.class);
    }

    @Test
    @DisplayName("존재하지 않는 어트리뷰트를 가져오면 null을 반환한다.")
    void getUserFromWhenNull() {
        assertThat(UserSession.getUserFrom(httpSession))
            .isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 어트리뷰트를 가져오면 null을 반환한다.")
    void getUserFromWhenNotNull() {
        when(httpSession.getAttribute("user"))
            .thenReturn(new User("1", "2", "3"));
        assertThat(UserSession.getUserFrom(httpSession)).isNotEmpty();
    }

    @Test
    @DisplayName("세션이 없다면 false 를 반환한다.")
    void isLoggedInWhenFalse() {
        assertThat(UserSession.isLoggedIn(httpSession)).isFalse();
    }

    @Test
    @DisplayName("세션이 있다면 true 를 반환한다.")
    void isLoggedInWhenTrue() {
        when(httpSession.getAttribute("user"))
            .thenReturn(new User("1", "2", "3"));
        assertThat(UserSession.isLoggedIn(httpSession)).isTrue();
    }
}