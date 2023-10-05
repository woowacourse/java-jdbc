package org.springframework.jdbc.core;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.List;

public class SingleResult {

    public static <T> T makeSingleResultFrom(final List<T> results){
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException();
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(results.size());
        }
        return results.iterator().next();
    }
}
