package com.interface21.dao.utils;

import com.interface21.dao.IncorrectResultSizeDataAccessException;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.Nullable;

public class DataAccessUtils {

    @Nullable
    public static <T> T nullableSingleResult(Collection<T> results) {
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        Iterator<T> it = results.iterator();
        return (it.hasNext() ? it.next() : null);
    }
}
