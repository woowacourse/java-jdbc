package com.interface21.jdbc.core.util;

public final class PrimitiveUtils {

    private PrimitiveUtils() {
    }

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

    public static Class<?> getWrapperType(Class<?> primitiveType) {
        if (!primitiveType.isPrimitive()) {
            throw new IllegalArgumentException("입력 타입이 기본형이 아닙니다: " + primitiveType);
        }
        return switch (primitiveType.getName()) {
            case "boolean" -> Boolean.class;
            case "byte" -> Byte.class;
            case "short" -> Short.class;
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "float" -> Float.class;
            case "double" -> Double.class;
            case "char" -> Character.class;
            default -> throw new IllegalArgumentException("지원하지 않는 기본형 타입입니다: " + primitiveType);
        };
    }
}
