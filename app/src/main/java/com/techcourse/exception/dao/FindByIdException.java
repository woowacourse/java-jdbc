package com.techcourse.exception.dao;

import com.techcourse.exception.AppException;

public class FindByIdException extends AppException {

    private static final String MESSAGE = "데이터 단일 조회 실패 - id";

    public FindByIdException() {
        this(MESSAGE);
    }

    private FindByIdException(String message) {
        super(message);
    }
}
