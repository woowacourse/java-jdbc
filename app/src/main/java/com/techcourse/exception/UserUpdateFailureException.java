package com.techcourse.exception;

import com.techcourse.domain.User;

public class UserUpdateFailureException extends RuntimeException {

    public UserUpdateFailureException(User user) {
        super(String.format("User update failed. attempted update id: %d", user.getId()));
    }
}
