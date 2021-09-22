package com.techcourse.exception.dao;

import com.techcourse.exception.AppException;

public class UpdateException extends AppException {

    private static final String MESSAGE = "데이터 수정 실패";

    public UpdateException() {
        this(MESSAGE);
    }

    private UpdateException(String message) {
        super(message);
    }
}
