package com.interface21.jdbc.core.conversion;

import java.util.ArrayList;
import java.util.List;

public class TypeConverterRegistry {
    private final List<TypeConverter> converters = new ArrayList<>();

    public void register(TypeConverter converter) {
        converters.add(converter);
    }

    public List<TypeConverter> getConverters() {
        return converters;
    }
}
