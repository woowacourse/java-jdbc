package com.interface21.jdbc.core;

import com.interface21.dao.DataNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;

public class GeneratedKeyHolder {

    private final Deque<Long> keys = new ArrayDeque<>();

    public Long getKey() {
        if (keys.isEmpty()) {
            throw new DataNotFoundException("키가 존재하지 않습니다.");
        }

        if (keys.size() != 1) {
            throw new IllegalStateException("키가 정상적인 개수가 아닙니다.");
        }
        return keys.getFirst();
    }

    public void addKey(Long key) {
        keys.offerLast(key);
    }
}
