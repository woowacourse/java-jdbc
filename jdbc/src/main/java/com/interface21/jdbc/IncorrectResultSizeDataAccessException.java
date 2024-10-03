package com.interface21.jdbc;

public class IncorrectResultSizeDataAccessException extends RuntimeException {

    public IncorrectResultSizeDataAccessException(String message) {
        super(message);
    }

    public IncorrectResultSizeDataAccessException() {
        super("조회 결과가 없습니다.");
    }

    public IncorrectResultSizeDataAccessException(int expectedSize, int actualSize) {
        super(String.format("%d개의 결과를 예상했지만 %d개의 결과가 조회되었습니다.", expectedSize, actualSize));
    }
}
