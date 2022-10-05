package com.techcourse.service.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {
        super("회원을 찾을 수 없습니다.");
    }
}
