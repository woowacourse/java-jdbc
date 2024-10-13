package com.techcourse.exception;

public class TechCourseApplicationException extends RuntimeException {

    public TechCourseApplicationException(String message) {
        super(message);
    }

    public TechCourseApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
