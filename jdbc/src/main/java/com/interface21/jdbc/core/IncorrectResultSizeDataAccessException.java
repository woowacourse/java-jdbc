package com.interface21.jdbc.core;

// https://github.com/spring-projects/spring-framework/blob/main/spring-tx/src/main/java/org/springframework/dao/IncorrectResultSizeDataAccessException.java
public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }

    public IncorrectResultSizeDataAccessException(int expectedSize) {
        super("Incorrect result size: expected " + expectedSize);
    }
}
