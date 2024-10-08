package com.interface21.dao.support;

import com.interface21.dao.DataAccessException;
import java.util.Collection;
import javax.annotation.Nullable;

public class DataAccessUtils {

    @Nullable
    public static <T> T getNullableSingleResult(@Nullable Collection<T> results) throws DataAccessException {
        if (results == null || results.isEmpty()) {
            throw new DataAccessException("Expected 1 result but found 0");
        }
        if (results.size() > 1) {
            throw new DataAccessException("Expected 1 result but found " + results.size());
        }
        return results.iterator().next();
    }
}
