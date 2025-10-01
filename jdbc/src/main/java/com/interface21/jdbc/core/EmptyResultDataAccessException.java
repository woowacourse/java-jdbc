package com.interface21.jdbc.core;

// https://github.com/spring-projects/spring-framework/blob/main/spring-tx/src/main/java/org/springframework/dao/EmptyResultDataAccessException.java
public class EmptyResultDataAccessException extends DataAccessException {

    public EmptyResultDataAccessException(String message) {
        super(message);
    }
}
