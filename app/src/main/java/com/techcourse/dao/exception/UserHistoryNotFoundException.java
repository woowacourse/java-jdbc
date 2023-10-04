package com.techcourse.dao.exception;

public class UserHistoryNotFoundException extends IllegalArgumentException {
    public UserHistoryNotFoundException(final String message) {
        super(message);
    }
}
