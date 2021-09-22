package com.techcourse.exception.dao;

import com.techcourse.exception.AppException;

public class FindAllException extends AppException {

    private static final String MESSAGE = "데이터 전체 조회 실패";

    public FindAllException() {
        this(MESSAGE);
    }

    private FindAllException(String message) {
        super(message);
    }
}
