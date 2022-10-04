package nextstep.jdbc;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveClassUtils {

    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new HashMap<>();

    static {
        primitiveTypeToWrapperMap.put(boolean.class, Boolean.class);
        primitiveTypeToWrapperMap.put(byte.class, Byte.class);
        primitiveTypeToWrapperMap.put(char.class, Character.class);
        primitiveTypeToWrapperMap.put(double.class, Double.class);
        primitiveTypeToWrapperMap.put(float.class, Float.class);
        primitiveTypeToWrapperMap.put(int.class, Integer.class);
        primitiveTypeToWrapperMap.put(long.class, Long.class);
        primitiveTypeToWrapperMap.put(short.class, Short.class);
        primitiveTypeToWrapperMap.put(void.class, Void.class);
    }

    public static Class<?> wrapPrimitiveClassIfNecessary(final Class<?> clazz) {
        if (clazz.isPrimitive() && clazz != void.class) {
            return primitiveTypeToWrapperMap.get(clazz);
        }
        return clazz;
    }
}
