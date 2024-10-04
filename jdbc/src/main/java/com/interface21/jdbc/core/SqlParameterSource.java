package com.interface21.jdbc.core;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlParameterSource {

    private static final Logger log = LoggerFactory.getLogger(SqlParameterSource.class);

    private final Object source;

    public SqlParameterSource(final Object source) {
        validateSourceIsNull(source);
        this.source = source;
    }

    private void validateSourceIsNull(final Object source) {
        if (source == null) {
            throw new IllegalArgumentException("SQL 파라미터 소스 객체는 null이 입력될 수 없습니다.");
        }
    }

    public Object getParameter(final String parameterName) {
        validateParameterName(parameterName);
        final Field parameter = parseFields().stream()
                .filter(field -> field.getName().equals(parameterName))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("일치하는 파라미터가 없습니다."));

        if (parameter.getType() != String.class) {
            throw new NoSuchElementException("해당 키 값을 가진 문자열 타입의 값이 존재하지 않습니다.");
        }

        return parseParameterValue(parameter);
    }

    private void validateParameterName(final String parameterName) {
        if (parameterName == null || parameterName.isBlank()) {
            throw new IllegalArgumentException("파라미터 이름으로 null 혹은 공백이 입력될 수 없습니다.");
        }
    }

    private List<Field> parseFields() {
        final Class<?> clazz = source.getClass();
        return List.of(clazz.getDeclaredFields());
    }

    private Object parseParameterValue(final Field field) {
        try {
            field.setAccessible(true);
            return field.get(source);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }
}
