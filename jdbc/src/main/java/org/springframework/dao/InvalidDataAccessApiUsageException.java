package org.springframework.dao;

public class InvalidDataAccessApiUsageException extends DataAccessException {

    public InvalidDataAccessApiUsageException() {
        super("쿼리의 파라미터 개수와 전달된 Arguments의 개수가 다릅니다.");
    }
}
