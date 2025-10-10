package com.interface21.jdbc.core.conversion;

import java.util.ArrayList;
import java.util.List;

public class TypeConverterRegistry {
    private static final List<TypeConverter> converters = new ArrayList<>();

    public static void register(TypeConverter converter) {
        converters.add(converter);
    }

    static List<TypeConverter> getConverters() {
        return converters;
    }
}
