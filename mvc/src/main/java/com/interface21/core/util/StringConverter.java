package com.interface21.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StringConverter {

    private static final Map<Class<?>, Function<String, ?>> converters = new HashMap<>();

    static {
        converters.put(String.class, Function.identity());
        converters.put(Integer.class, Integer::parseInt);
        converters.put(Long.class, Long::parseLong);
        converters.put(int.class, str -> str == null ? 0 : Integer.parseInt(str));
        converters.put(long.class, str -> str == null ? 0L : Long.parseLong(str));
    }

    private StringConverter() {
    }

    public static Object convert(Class<?> type, String value) {
        return converters.get(type).apply(value);
    }
}
