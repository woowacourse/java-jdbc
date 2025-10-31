package com.interface21.transaction.support;

import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<Object, Object>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static void init() {
        if (resources.get() == null) {
            resources.set(new HashMap<>());
        }
    }

    public static void bindResource(Object key, Object value) {
        init();
        resources.get().put(key, value);
    }

    public static Object getResource(Object key) {
        Map<Object, Object> map = resources.get();
        return (map != null) ? map.get(key) : null;
    }

    public static void unbindResource(Object key) {
        Map<Object, Object> map = resources.get();
        if (map != null) {
            map.remove(key);
            if (map.isEmpty()) {
                resources.remove();
            }
        }
    }

    public static boolean hasResource(Object key) {
        Map<Object, Object> map = resources.get();
        return (map != null) && map.containsKey(key);
    }
}
