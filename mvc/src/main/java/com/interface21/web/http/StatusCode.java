package com.interface21.web.http;

public enum StatusCode {

    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);

    private final int value;

    StatusCode(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public boolean is4xxClientError() {
        return value >= 400 && value < 500;
    }

    public boolean is5xxServerError() {
        return value >= 500;
    }
}
