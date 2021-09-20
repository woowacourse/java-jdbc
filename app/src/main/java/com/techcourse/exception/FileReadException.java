package com.techcourse.exception;

public class FileReadException extends RuntimeException {

    public FileReadException(Throwable cause) {
        super(cause.getMessage());
    }
}
