package com.interface21.jdbc.core;

/**
 * 조회 결과의 크기가 예상과 다를 때 발생하는 예외
 *
 * @see <a href="https://github.com/spring-projects/spring-framework/blob/main/spring-tx/src/main/java/org/springframework/dao/IncorrectResultSizeDataAccessException.java">Spring Framework IncorrectResultSizeDataAccessException</a>
 */
public class IncorrectResultSizeDataAccessException extends DataAccessException {

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }

    public IncorrectResultSizeDataAccessException(int expectedSize) {
        super("Incorrect result size: expected " + expectedSize);
    }
}
