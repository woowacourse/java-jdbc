package com.techcourse.exception;

public class MoreThanTwoResultsFoundFromOptionalMethodException extends RuntimeException {
    private static final String MESSAGE_FORMAT = "Optional을 반환하는 메소드에서 적어도 2개 이상의 결과 값이 발생했습니다. (결과 개수: %d)";

    public MoreThanTwoResultsFoundFromOptionalMethodException(int size) {
        super(String.format(MESSAGE_FORMAT, size));
    }
}
