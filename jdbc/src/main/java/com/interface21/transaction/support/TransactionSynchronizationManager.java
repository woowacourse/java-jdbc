package com.interface21.transaction.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<Object, Object>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Object getResource(final Object key) {
        final Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static void bindResource(
            final Object key,
            final Object value
    ) {
        Objects.requireNonNull(value, "Value는 null일 수 없습니다.");
        Map<Object, Object> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }

    public static Object unbindResource(final Object key) {
        final Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        final Object value = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        return value;
    }
}
