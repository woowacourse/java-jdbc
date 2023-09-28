package org.springframework.jdbc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectConverter {

    private ObjectConverter() {
    }

    public static <T> T convert(ResultSet resultSet, Class<T> type) {
        try {
            Constructor<T> constructor = type.getConstructor();
            T result = constructor.newInstance();
            for (Field field : result.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                field.set(result, resultSet.getObject(fieldName, field.getType()));
            }
            return result;
        } catch (NoSuchMethodException |
                 InvocationTargetException |
                 InstantiationException |
                 SQLException |
                 IllegalAccessException e) {
            throw new ResultSetConvertException("ResultSet 을 변환하는데 실패했습니다.", e);
        }
    }
}
