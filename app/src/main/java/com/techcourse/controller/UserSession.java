package com.techcourse.controller;

import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import com.techcourse.domain.User;

public class UserSession {

    public static final String SESSION_KEY = "user";

    private UserSession() {
    }

    public static Optional<User> getUserFrom(final HttpSession session) {
        final var user = (User) session.getAttribute(SESSION_KEY);
        return Optional.ofNullable(user);
    }

    public static boolean isLoggedIn(final HttpSession session) {
        return getUserFrom(session).isPresent();
    }
}
