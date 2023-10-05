package org.springframework.jdbc.core.result;

import org.springframework.jdbc.exception.NotSingleResultException;

import java.util.List;

public class ResultValidator {

    public static <T> void validateSingleResult(List<T> results) {
        final int resultSize = results.size();
        if (resultSize != 1) {
            throw new NotSingleResultException("결과값이 1개가 아닙니다.");
        }
    }
}
