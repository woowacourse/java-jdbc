package com.techcourse.controller;

import com.techcourse.domain.User;
import com.techcourse.exception.UnauthorizedException;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

public class UserSession {

    public static final String SESSION_KEY = "user";

    private UserSession() {}

    public static boolean isAlreadyLogin(HttpSession session) {
        return getUserFrom(session).isPresent();
    }

    public static User getUser(HttpSession session) {
        return getUserFrom(session).orElseThrow(() -> new UnauthorizedException("세션을 통한 유저 조회에 실패했습니다."));
    }

    private static Optional<User> getUserFrom(HttpSession session) {
        final User user = (User) session.getAttribute(SESSION_KEY);
        return Optional.ofNullable(user);
    }
}
