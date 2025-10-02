package com.interface21.jdbc.core;

/**
 * 조회 결과가 비어있을 때 발생하는 예외
 *
 * @see <a href="https://github.com/spring-projects/spring-framework/blob/main/spring-tx/src/main/java/org/springframework/dao/EmptyResultDataAccessException.java">Spring Framework EmptyResultDataAccessException</a>
 */
public class EmptyResultDataAccessException extends DataAccessException {

    public EmptyResultDataAccessException(String message) {
        super(message);
    }
}
