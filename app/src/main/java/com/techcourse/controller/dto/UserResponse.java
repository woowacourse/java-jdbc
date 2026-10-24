package com.techcourse.controller.dto;

import com.techcourse.domain.User;

public record UserResponse(long id, String account, String email) {

    public static UserResponse from(final User user) {
        return new UserResponse(user.getId(), user.getAccount(), user.getEmail());
    }
}
