package com.interface21.jdbc.core.util;

public final class PrimitiveUtils {

    private PrimitiveUtils() {}

    public static Object getDefaultValue(Class<?> type) {
        return switch (type.getName()) {
            case "boolean" -> false;
            case "byte" -> (byte) 0;
            case "short" -> (short) 0;
            case "int" -> 0;
            case "long" -> 0L;
            case "float" -> 0.0f;
            case "double" -> 0.0d;
            case "char" -> '\u0000';
            default -> throw new IllegalArgumentException("Unsupported primitive type: " + type);
        };
    }
}
