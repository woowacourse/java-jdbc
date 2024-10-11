package com.techcourse.domain;

import java.time.LocalDateTime;

public record UserHistory(
        Long id,
        long userId,
        String account,
        String password,
        String email,
        LocalDateTime createdAt,
        String createdBy
) {

    public UserHistory(final User user, final String createdBy) {
        this(
                null,
                user.id(),
                user.account(),
                user.password(),
                user.email(),
                LocalDateTime.now(),
                createdBy
        );
    }
}
