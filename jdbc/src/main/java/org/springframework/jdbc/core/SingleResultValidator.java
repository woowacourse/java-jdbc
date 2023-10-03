package org.springframework.jdbc.core;

import org.springframework.jdbc.NotSingleResultException;

import java.util.List;

public class SingleResultValidator {

    public static <T> void validate(List<T> results) {
        final int resultSize = results.size();
        if (resultSize > 1 || resultSize == 0) {
            throw new NotSingleResultException("결과값이 1개가 아닙니다.");
        }
    }
}
