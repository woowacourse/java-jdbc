package com.techcourse.exception.dao;

import com.techcourse.exception.AppException;

public class InsertException extends AppException {

    private static final String MESSAGE = "데이터 삽입 실패";

    public InsertException() {
        this(MESSAGE);
    }

    private InsertException(String message) {
        super(message);
    }
}
