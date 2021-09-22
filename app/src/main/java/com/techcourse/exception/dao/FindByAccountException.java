package com.techcourse.exception.dao;

import com.techcourse.exception.AppException;

public class FindByAccountException extends AppException {

    private static final String MESSAGE = "데이터 단일 조회 실패 - account";

    public FindByAccountException() {
        this(MESSAGE);
    }

    private FindByAccountException(String message) {
        super(message);
    }
}
