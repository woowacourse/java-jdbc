package com.interface21.transaction.support;

import java.util.HashMap;
import java.util.Map;

public class ThreadMap<T, R> {

    private final ThreadLocal<Map<T, R>> resources;

    public ThreadMap(Map<T, R> resources) {
        this.resources = ThreadLocal.withInitial(() -> resources);
    }

    public ThreadMap() {
        this.resources = ThreadLocal.withInitial(HashMap::new);
    }

    public R getMap(T key) {
        return getMap().get(key);
    }

    public void put(T key, R value) {
        getMap().put(key, value);
    }

    public R remove(T key) {
        return getMap().remove(key);
    }

    public Map<T, R> getMap() {
        return resources.get();
    }
}
