package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.util.Collection;

public abstract class DataAccessUtils {

    private DataAccessUtils() {}

    public static <T> T singleResult(Collection<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("조회된 데이터가 존재하지 않습니다.");
        }
        if (result.size() > 1) {
            throw new DataAccessException("조회된 데이터가 2개 이상입니다.");
        }
        return result.iterator().next();
    }
}
