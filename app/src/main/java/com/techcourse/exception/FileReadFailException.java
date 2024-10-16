package com.techcourse.exception;

public class FileReadFailException extends RuntimeException {

    public FileReadFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
