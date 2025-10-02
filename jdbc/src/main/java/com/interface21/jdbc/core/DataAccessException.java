package com.interface21.jdbc.core;

/**
 * 데이터 접근 중 발생하는 예외의 최상위 클래스
 * <p>
 * SQLException과 같은 Checked Exception을 Unchecked Exception으로 변환하여
 * 예외 처리를 선택적으로 만듭니다.
 *
 * @see <a href="https://github.com/spring-projects/spring-framework/blob/main/spring-tx/src/main/java/org/springframework/dao/DataAccessException.java">Spring Framework DataAccessException</a>
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(Throwable cause) {
        super(cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
