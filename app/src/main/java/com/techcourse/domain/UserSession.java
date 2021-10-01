package com.techcourse.domain;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

public class UserSession {

    public static final String SESSION_KEY = "user";

    private UserSession() {
    }

    public static Optional<User> getUserFrom(HttpSession session) {
        final User user = (User) session.getAttribute(SESSION_KEY);
        return Optional.ofNullable(user);
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getUserFrom(session).isPresent();
    }
}
