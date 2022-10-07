package com.techcourse.support.jdbc.init;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("존재하지 않는 자원입니다");
    }
}
