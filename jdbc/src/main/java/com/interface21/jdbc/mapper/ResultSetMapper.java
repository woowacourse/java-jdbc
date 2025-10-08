package com.interface21.jdbc.mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Arrays;

public class ResultSetMapper {

    // Reflection을 활용한 객체 매핑.
    // 클래스의 필드 순서와 생성자의 파라미터 순서가 다르면 작동 안함.
    public static <T> T map(ResultSet rs, Class<T> clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        Class<?>[] fieldTypes = Arrays.stream(fields)
                .map(Field::getType)
                .toArray(Class<?>[]::new);

        Constructor<T> constructor = clazz.getDeclaredConstructor(fieldTypes);
        Object[] constructorArgs = new Object[fields.length];

        for (int i = 0; i < fields.length; i++) {
            constructorArgs[i] = rs.getObject(i + 1);
        }

        return constructor.newInstance(constructorArgs);
    }
}
