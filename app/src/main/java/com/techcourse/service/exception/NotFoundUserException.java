package com.techcourse.service.exception;

public class NotFoundUserException extends RuntimeException {

    public NotFoundUserException() {
        super("찾으려는 회원이 존재하지 않습니다.");
    }
}
