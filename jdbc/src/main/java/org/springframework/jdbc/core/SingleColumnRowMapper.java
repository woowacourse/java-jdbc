package org.springframework.jdbc.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nullable;

public class SingleColumnRowMapper<T> implements RowMapper<T> {

    private final Class<T> requiredType;

    public SingleColumnRowMapper(final Class<T> requiredType) {
        this.requiredType = requiredType;
    }

    @Nullable
    @Override
    public T mapRow(final ResultSet resultSet, final int rowNumber) throws SQLException {
        try {
            final Constructor<T> constructor = getConstructor();
            final T result = constructor.newInstance();

            for (final Field field : requiredType.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(result, resultSet.getObject(field.getName(), field.getType()));
            }

            return result;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("변환할 수 없는 객체입니다.");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("기본 생성자가 존재하지 않습니다.");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("접근할 수 없는 필드입니다.");
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("인스턴스를 생성할 수 없습니다.");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("생성자를 호출할 수 없습니다.");
        }
    }

    private Constructor<T> getConstructor() throws NoSuchMethodException {
        final Constructor<T> constructor = requiredType.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor;
    }
}
